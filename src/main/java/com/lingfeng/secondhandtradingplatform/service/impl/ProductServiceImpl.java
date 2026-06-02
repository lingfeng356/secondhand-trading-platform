package com.lingfeng.secondhandtradingplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingfeng.secondhandtradingplatform.DTO.request.*;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.response.ProductDetailResponse;
import com.lingfeng.secondhandtradingplatform.converter.ProductConverter;
import com.lingfeng.secondhandtradingplatform.mapper.*;
import com.lingfeng.secondhandtradingplatform.pojo.*;
import com.lingfeng.secondhandtradingplatform.rabbitProducer.ProductRabbitProducer;
import com.lingfeng.secondhandtradingplatform.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.lingfeng.secondhandtradingplatform.constant.SystemConstant.*;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductServiceImpl extends ServiceImpl<ProductMapper,Product> implements ProductService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CollectMapper collectMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Value("${upload.path}")
    private String uploadPath; // 例如: /uploads/avatars/

    @Autowired
    private ProductRabbitProducer producer;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductConverter productConverter;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrderMapper orderMapper;

    //发布商品
    @Override
    public Result<Void> publishProduct(Long userId, ProductPublishRequest ppr) {

        log.info("发布商品请求:userId={}",userId);

        //将DTO转换成entity
        Product product = productConverter.toEntity(ppr);

        //存数据库
        product.setStatus(PRODUCT_STATUS_ONSALE);
        product.setUserId(userId);
        save(product);

        //Mybatis-plus保存完数据会自动将数据赋值给对象
        Long productId = product.getId();

        //缓存redis
        String key = PRODUCT_KEY + productId;
        Map<String,Object> tempMap = BeanUtil.beanToMap(product);
        Map<String,String> productMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
            if (entry.getValue() != null) {
                productMap.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        try {
            stringRedisTemplate.opsForHash().putAll(key,productMap);
            stringRedisTemplate.expire(key,PRODUCT_TTL, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("缓存redis失败:未知原因,不影响主流程");
        }

        //返回结果
        log.info("发布商品成功:userId={},ProductId={}",userId,productId);
        return Result.success();
    }

    //TODO:缓存击穿和缓存穿透，重复代码
    //查看商品详情
    @Override
    public Result<ProductDetailResponse> productDetail(Long productId) {

        log.info("查询商品请求:productId={}",productId);

        //校验id
        if(productId == null || productId <= 0){
            log.warn("查询商品失败:商品不存在,productId={}",productId);
            return Result.error(404,"商品不存在");
        }

        //从redis查
        String key = PRODUCT_KEY + productId;

        //将对象从hashmap转成json再转成ProductDetailResponse对象
        Map<Object,Object> tmpMap = stringRedisTemplate.opsForHash().entries(key);

        //如果不为空和不为null,直接返回结果
        if(!tmpMap.isEmpty()){
            String json = JSONUtil.toJsonStr(tmpMap);
            Product product = JSONUtil.toBean(json, Product.class);

            //防止缓存穿透
            if(product == null){
                log.warn("查询商品失败:商品不存在,productId={}",productId);
                return Result.error(404,"商品不存在");
            }

            producer.sendViewMessage(productId);

            ProductDetailResponse pdr = productConverter.toProductDetailResponse(product);

            //查询user，为注入卖家信息准备
            User user = userMapper.selectById(product.getUserId());
            pdr.setSellerName(user.getUsername());
            pdr.setSellerImg(user.getImg());

            log.info("查询商品成功:productId={}",productId);
            return Result.success(pdr);
        }

        //没有则去数据库查
        Product product = productMapper.selectById(productId);

        //如果为null则说明数据库没有该商品数据
        if(product == null){
            Map<String, String> emptyCache = new HashMap<>();
            emptyCache.put("null", "1");
            stringRedisTemplate.opsForHash().putAll(key, emptyCache);
            stringRedisTemplate.expire(key, 5, TimeUnit.MINUTES);
            log.warn("查询商品失败:商品不存在,productId={}",productId);
            return Result.error(404,"商品不存在");
        }

        producer.sendViewMessage(productId);

        //封装响应DTO
        ProductDetailResponse pdr = productConverter.toProductDetailResponse(product);

        //查询user，为注入卖家信息准备
        User user = userMapper.selectById(product.getUserId());
        pdr.setSellerName(user.getUsername());
        pdr.setSellerImg(user.getImg());

        //缓存redis
        //将product对象转成map再将内部所有属性转换成String类型
        Map<String,Object> tempMap = BeanUtil.beanToMap(product);
        Map<String,String> productMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
            if (entry.getValue() != null) {
                productMap.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        //缓存到redis中
        stringRedisTemplate.opsForHash().putAll(key,productMap);
        stringRedisTemplate.expire(key,PRODUCT_TTL, TimeUnit.MINUTES);

        //返回信息
        log.info("查询商品成功:productId={}",productId);
        return Result.success(pdr);
    }

    //编辑商品信息
    @Override
    public Result<Void> productUpdate(Long userId, UpdateProductDetailRequest updr, Long productId) {

        log.info("编辑商品请求:userId={},productId={}",userId,productId);

        //校验id是否合法
        if(productId == null || productId <= 0){
            log.warn("编辑商品失败:商品id无效,productId={}",productId);
            return Result.error(404,"商品id无效");
        }

        Product product = getById(productId);

        //判断product是否存在
        if(product == null){
            log.warn("编辑商品失败:商品不存在,productId={}",productId);
            return Result.error(404,"商品不存在");
        }

        Long realUserId = product.getUserId();
        if(!realUserId.equals(userId)){
            log.warn("编辑商品失败:无修改权限,userId={},productId={}",userId,productId);
            return Result.error(403,"无修改权限");
        }

        //更新数据库
        //因为前端传来的product没有id，需要手动传参，到数据库后会自动更改非null的数据
        Product pct = productConverter.toEntity(updr);
        pct.setId(productId);
        updateById(pct);

        try {
            //删除redis缓存
            stringRedisTemplate.delete(PRODUCT_KEY + productId);
        } catch (Exception e) {
            log.warn("删除redis失败:未知原因,不影响主流程");
        }

        //返回成功信息
        log.info("编辑商品成功:userId={},productId={}",userId,productId);
        return Result.success();
    }

    //下架商品
    @Override
    public Result<Void> removeProduct(Long userId, Long productId) {

        log.info("下架商品请求:userId={},productId={}",userId,productId);

        //校验id是否合法
        if(productId == null || productId <= 0){
            log.warn("下架商品失败:商品id无效,productId={}",productId);
            return Result.error(400,"商品id无效");
        }

        //获取userId
        Product product = getById(productId);

        //判断product是否存在
        if(product == null){
            log.warn("下架商品失败:商品不存在,productId={}",productId);
            return Result.error(404,"商品不存在");
        }

        if(!product.getStatus().equals(PRODUCT_STATUS_ONSALE)){
            log.warn("下架商品失败:商品当前状态无法修改,userId={},productId={}",userId,productId);
            return Result.error(400,"商品当前状态无法修改");
        }

        //鉴权
        Long realUserId = product.getUserId();
        if(!realUserId.equals(userId)){
            log.warn("下架商品失败:无权限修改,userId={},productId={}",userId,productId);
            return Result.error(403,"无权限修改");
        }

        //数据库查询商品
        //不为null则修改status
        product.setStatus(PRODUCT_STATUS_OFFSALE);

        //将修改后的商品保存到数据库
        updateById(product);

        try {
            //删除redis缓存
            stringRedisTemplate.delete(PRODUCT_KEY + productId);
        } catch (Exception e) {
            log.warn("删除redis失败:未知原因,不影响主流程");
        }

        //返回结果
        log.info("下架商品成功:userId={},productId={}",userId,productId);
        return Result.success();
    }

    //删除商品
    @Override
    public Result<Void> deleteProduct(Long userId, Long productId) {

        log.info("删除商品请求:userId={},productId={}",userId,productId);

        //校验id是否合法
        if(productId == null || productId <= 0){
            log.warn("删除商品失败:商品id无效,productId={}",productId);
            return Result.error(400,"商品id无效");
        }

        //获取userId
        Product product = getById(productId);

        //判断product是否存在
        if(product == null){
            log.warn("删除商品失败:商品不存在,productId={}",productId);
            return Result.error(404,"商品不存在");
        }

        //鉴权
        Long realUserId = product.getUserId();
        if(!realUserId.equals(userId)){
            log.warn("删除商品失败:无权限修改,userId={},productId={}",userId,productId);
            return Result.error(403,"无权限修改");
        }

        //删除商品
        removeById(product);

        try {
            //删除redis缓存
            stringRedisTemplate.delete(PRODUCT_KEY + productId);
        } catch (Exception e) {
            log.warn("删除redis失败:未知原因,不影响主流程");
        }

        //返回结果
        log.info("删除商品成功:userId={},productId={}",userId,productId);
        return Result.success();
    }

    //将已下架的商品重新上架
    @Override
    public Result<Void> republishProduct(Long userId, Long productId) {

        log.info("重新上架商品请求:userId={},productId={}",userId,productId);

        //校验id是否合法
        if(productId == null || productId <= 0){
            log.warn("重新上架商品失败:商品id无效,productId={}",productId);
            return Result.error(400,"商品id无效");
        }

        //获取userId
        Product product = getById(productId);

        //判断product是否存在
        if(product == null){
            log.warn("重新上架商品失败:商品不存在,productId={}",productId);
            return Result.error(404,"商品不存在");
        }

        if(!product.getStatus().equals(PRODUCT_STATUS_OFFSALE)){
            log.warn("重新上架商品失败:商品当前状态无法修改,userId={},productId={}",userId,productId);
            return Result.error(400,"商品当前状态无法修改");
        }

        //鉴权
        Long realUserId = product.getUserId();
        if(!realUserId.equals(userId)){
            log.warn("重新上架商品失败:无权限修改,userId={},productId={}",userId,productId);
            return Result.error(403,"无权限修改");
        }

        //数据库查询商品
        //不为null则修改status
        product.setStatus(PRODUCT_STATUS_ONSALE);

        //将修改后的商品保存到数据库
        updateById(product);

        try {
            //删除redis缓存
            stringRedisTemplate.delete(PRODUCT_KEY + productId);
        } catch (Exception e) {
            log.warn("删除redis失败:未知原因,不影响主流程");
        }

        //返回结果
        log.info("重新上架商品成功:userId={},productId={}",userId,productId);
        return Result.success();
    }

    //我的发布商品
    @Override
    public Result<IPage<Product>> showMyList(Long userId,PageRequest pageRequest) {

        log.info("查询我的发布请求:userId={}",userId);

        Integer pageNum = pageRequest.getPageNum();
        Integer pageSize = pageRequest.getPageSize();

        // 直接分页查询
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getUserId, userId)
                .orderByDesc(Product::getCreateTime);
        page(page, wrapper);

        //返回结果
        return Result.success(page);
    }

    //查询商品列表
    @Override
    public Result<IPage<Product>> showProductList(ProductListRequest productListRequest) {

        log.info("查询商品列表请求");

        //构建分页对象
        int pageNum = productListRequest.getPageNum() == null ? 1 : productListRequest.getPageNum();
        int pageSize = productListRequest.getPageSize() == null ? 10 : productListRequest.getPageSize();
        Page<Product> page = new Page<>(pageNum,pageSize);

        //构建查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        //分类筛选
        if(StringUtils.hasText(productListRequest.getCategory())){
            wrapper.eq(Product::getCategory,productListRequest.getCategory());//mybatis-plus会将这个方法引用转变成category字符串
        }

        //价格范围筛选(qs:如果都为null会怎么样子)
        if(productListRequest.getMinPrice() != null){
            wrapper.ge(Product::getPrice,productListRequest.getMinPrice());
        }
        if(productListRequest.getMaxPrice() != null){
            wrapper.le(Product::getPrice,productListRequest.getMaxPrice());
        }

        //城市筛选
        if(StringUtils.hasText(productListRequest.getCity())){
            wrapper.eq(Product::getCity,productListRequest.getCity());
        }

        //过滤掉非上架状态的商品
        wrapper.eq(Product::getStatus,PRODUCT_STATUS_ONSALE);

        //搜索栏模糊查询
        if(StringUtils.hasText(productListRequest.getTitle())){
            wrapper.like(Product::getTitle,productListRequest.getTitle());
        }


        //排序
        String sortBy = productListRequest.getSortBy();
        String sortOrder = productListRequest.getSortOrder();

        if ("price".equals(sortBy)) {
            if ("asc".equalsIgnoreCase(sortOrder)) {
                wrapper.orderByAsc(Product::getPrice);
            } else {
                wrapper.orderByDesc(Product::getPrice);
            }
        } else if ("createTime".equals(sortBy)) {
            if ("asc".equalsIgnoreCase(sortOrder)) {
                wrapper.orderByAsc(Product::getCreateTime);
            } else {
                wrapper.orderByDesc(Product::getCreateTime);
            }
        } else {
            // 默认按创建时间倒序
            wrapper.orderByDesc(Product::getCreateTime);
        }

        //执行分页查询
        Page<Product> result = productMapper.selectPage(page,wrapper);

        //返回结果
        log.info("查询商品列表成功");
        return Result.success(result);
    }

    //根据热度推荐首页商品
    @Override
    public Result<IPage<Product>> recommendProducts(PageRequest pageRequest) {

        log.info("推荐首页商品请求");

        Integer pageNum = pageRequest.getPageNum();
        Integer pageSize = pageRequest.getPageSize();

        String cacheKey = PRODUCT_HOT_LIST + ":" + pageNum + ":" + pageSize;

        String cacheJson = stringRedisTemplate.opsForValue().get(cacheKey);
        if(StringUtils.hasText(cacheJson)){
            //反序列化
            Page<Product> cachePage = JSONUtil.toBean(cacheJson, new TypeReference<Page<Product>>() {}, false);
            log.info("推荐首页商品成功");
            return Result.success(cachePage);
        }

        Page<Product> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus,PRODUCT_STATUS_ONSALE)
                .orderByDesc(Product::getViewCount)   // 浏览量
                .orderByDesc(Product::getLikeCount)   // 点赞量
                .orderByDesc(Product::getCreateTime); // 最新

        page(page,wrapper);

        try {
            //转成json格式存入redis缓存
            String jsonData = JSONUtil.toJsonStr(page);
            stringRedisTemplate.opsForValue().set(cacheKey,jsonData,PRODUCT_HOT_LIST_TTL,TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入缓存失败:未知原因");
        }

        log.info("推荐首页商品成功");
        return Result.success(page);
    }

    //分类展示商品
    @Override
    public Result<IPage<Product>> showByCategory(ShowProductByCategoryRequest request) {

        log.info("分类展示商品请求");

        Integer pageNum = request.getPageNum();
        Integer pageSize = request.getPageSize();
        String category = request.getCategory();

        if(category == null){
            log.error("分类展示商品失败:分类为空");
            return Result.error(400,"分类为空");
        }

        Page<Product> page = new Page<>(pageNum,pageSize);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Product::getCategory,category)
                .eq(Product::getStatus,PRODUCT_STATUS_ONSALE)
                .orderByAsc(Product::getCreateTime);

        page(page,wrapper);

        log.info("分类展示商品成功");
        return Result.success(page);
    }

    //TODO:如果有旧的图片，需要删除旧的图片，防止硬盘浪费
    //上传商品图片
    @Override
    public Result<Void> upload(MultipartFile file, Long userId, Long productId) {

        log.info("上传商品图片请求:userId={},productId={}",userId,productId);

        //权限验证
        Product product = getById(productId);

        if(product == null){
            log.error("上传商品图片失败:商品不存在,userId={},productId={}",userId,productId);
            return Result.error(404,"商品不存在");
        }

        if(!userId.equals(product.getUserId())){
            log.error("上传商品图片失败:无权限,userId={},productId={}",userId,productId);
            return Result.error(403,"无权限");
        }


        try {
            // 1. 校验文件
            if (file == null || file.isEmpty()) {
                log.error("上传商品图片失败:文件不存在,userId={},productId={}",userId,productId);
                return Result.error(404,"文件不能为空");
            }

            // 2. 校验文件类型（只允许图片）
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.error("上传商品图片失败:文件类型错误,userId={},productId={}",userId,productId);
                return Result.error(400,"只能上传图片文件");
            }

            // 3. 校验文件大小（例如限制 5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                log.error("上传商品图片失败:文件过大,userId={},productId={}",userId,productId);
                return Result.error(400,"文件大小不能超过5MB");
            }

            // 4. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = userId + "_" + System.currentTimeMillis() + extension;

            // 5. 创建目录（如果不存在）
            File destDir = new File(uploadPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            // 6. 保存文件到本地
            File destFile = new File(destDir, newFileName);
            file.transferTo(destFile);

            // 7. 存储相对路径到数据库（用于前端访问）
            String dbPath = "/static/" + newFileName;  // 根据你的访问路径调整

            product.setImages(dbPath);
            updateById(product);

            log.info("上传商品图片成功:userId={},productId={}",userId,productId);
            return Result.success();

        } catch (IOException e) {
            log.error("上传商品图片失败:发生未知错误,userId={},productId={}",userId,productId);
            return Result.error(400,"上传失败: " + e.getMessage());
        }
    }

    //点赞商品
    //TODO：并发安全，优化性能
    @Override
    public Result<Void> likeProduct(Long userId,Long productId) {

        log.info("点赞商品请求:userId={},productId={}",userId,productId);

        if(productId == null){
            log.warn("点赞商品失败:商品不存在,userId={},productId={}",userId,productId);
            return Result.error(404,"商品不存在");
        }

        producer.sendLikeMessage(userId,productId);

        log.info("点赞商品完成:userId={},productId={}",userId,productId);
        return Result.success();
    }

    //取消点赞
    @Override
    public Result<Void> cancelLikeProduct(Long userId, Long productId) {
        log.info("取消点赞商品请求:userId={},productId={}",userId,productId);

        if(productId == null){
            log.warn("取消点赞商品失败:商品不存在,userId={},productId={}",userId,productId);
            return Result.error(404,"商品不存在");
        }

        String key = PRODUCT_LIKE_KEY + productId;

        Long removed = stringRedisTemplate.opsForSet().remove(key,userId.toString());

        if(removed != null && removed > 0){
            log.info("取消点赞商品完成:userId={},productId={}",userId,productId);
            return Result.success();
        }else{
            log.warn("取消点赞失败: 未点过赞, userId={}, productId={}", userId, productId);
            return Result.error(400, "尚未点赞");
        }

    }

    //检查是否点赞
    @Override
    public Result<Boolean> checkIsLike(Long userId, Long productId) {
        log.info("检查是否点赞请求:userId={},productId={}",userId,productId);

        if(productId == null){
            log.warn("检查是否点赞失败:商品不存在,userId={},productId={}",userId,productId);
            return Result.error(404,"商品不存在");
        }

        String key = PRODUCT_LIKE_KEY + productId;

        Boolean isLiked = stringRedisTemplate.opsForSet().isMember(key,userId.toString());

        if(!isLiked){
            log.info("检查是否点赞成功:未点赞,userId={},productId={}",userId,productId);
            return Result.success(false);
        }

        log.info("检查是否点赞成功:已点赞,userId={},productId={}",userId,productId);
        return Result.success(true);
    }

    //收藏商品
    //TODO：改定时异步更新，还有其他问题问ai
    @Override
    public Result<Void> collectProduct(Long userId, Long productId) {
        log.info("收藏商品请求:userId={},productId={}",userId,productId);

        if(productId == null){
            log.warn("收藏商品失败:商品不存在,userId={},productId={}",userId,productId);
            return Result.error(404,"商品不存在");
        }

        //联合唯一索引判断是否重复并抛出异常
        try {
            Collect collect = new Collect();
            collect.setProductId(productId);
            collect.setUserId(userId);
            LocalDateTime time = LocalDateTime.now();
            collect.setCreateTime(time);

            //存数据库
            collectMapper.insert(collect);

            //存redis
            String key = PRODUCT_COLLECT_KEY + productId;
            stringRedisTemplate.opsForSet().add(key,userId.toString());

            Product product = getById(productId);
            Integer newCollectCount = product.getCollectCount() + 1;
            product.setCollectCount(newCollectCount);
            updateById(product);

            log.info("收藏商品成功:userId={},productId={}",userId,productId);
            return Result.success();
        } catch (DuplicateKeyException e) {
            log.warn("收藏商品失败:userId={},productId={}",userId,productId);
            return Result.error(400,"重复收藏");
        }
    }

    //取消收藏
    @Override
    public Result<Void> cancelCollect(Long userId, Long productId) {
        log.info("取消收藏商品请求:userId={},productId={}",userId,productId);

        if(productId == null){
            log.warn("取消收藏商品失败:商品不存在,userId={},productId={}",userId,productId);
            return Result.error(404,"商品不存在");
        }

        String key = PRODUCT_COLLECT_KEY + productId;

        //判断是否收藏
        Boolean isCollected = stringRedisTemplate.opsForSet().isMember(key,userId.toString());

        if(!isCollected){
            log.warn("取消收藏商品失败:未收藏,userId={},productId={}",userId,productId);
            return Result.error(400,"未收藏");
        }

         //先删redis后删数据库
        stringRedisTemplate.opsForSet().remove(key,userId.toString());

        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId,userId)
                .eq(Collect::getProductId,productId);
        collectMapper.delete(wrapper);

        Product product = getById(productId);
        Integer newCollectCount = product.getCollectCount() - 1;
        product.setCollectCount(newCollectCount);
        updateById(product);

        log.info("取消收藏商品成功:userId={},productId={}",userId,productId);
        return Result.success();
    }

    //确认是否收藏
    @Override
    public Result<Boolean> checkIsCollect(Long userId, Long productId) {
        log.info("确认收藏商品请求:userId={},productId={}",userId,productId);

        if(productId == null){
            log.warn("确认收藏商品失败:商品不存在,userId={},productId={}",userId,productId);
            return Result.error(404,"商品不存在");
        }

        //先从redis查后从数据库查
        String key = PRODUCT_COLLECT_KEY + productId;
        Boolean isCollect = stringRedisTemplate.opsForSet().isMember(key,userId.toString());

        if(isCollect){
            log.info("确认收藏商品请求成功:已收藏,userId={},productId={}",userId,productId);
            return Result.success(true);
        }

        //去数据库查
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId,userId)
                .eq(Collect::getProductId,productId);
        Collect collect = collectMapper.selectOne(wrapper);

        if(collect == null){
            log.info("确认收藏商品请求成功:未收藏,userId={},productId={}",userId,productId);
            return Result.success(false);
        }

        log.info("确认收藏商品请求成功:已收藏,userId={},productId={}",userId,productId);
        return Result.success(true);
    }

    //查询我的收藏商品
    @Override
    public Result<Page<Product>> myCollectProducts(Long userId, PageRequest pageRequest) {
        log.info("查看我的收藏商品请求:userId={}",userId);

        Integer pageNum = pageRequest.getPageNum();
        Integer pageSize = pageRequest.getPageSize();

        Page<Product> page = new Page<>(pageNum,pageSize);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.inSql(Product::getId,"select product_id from collect where user_id = " + userId + " order by create_time desc");

        productMapper.selectPage(page,wrapper);

        return Result.success(page);
    }

    //OK
    //发布评价
    @Override
    public Result<Void> publishReview(Long userId, PublishReviewRequest request) {
        Long productId = request.getProductId();
        String content = request.getContent();
        Integer rating = request.getRating();
        String orderId = request.getOrderId();

        log.info("发布评价请求:userId={},productId={}",userId,productId);

        //鉴权
        Order order = orderMapper.selectById(orderId);

        if(order == null){
            log.warn("发布评价失败:订单不存在,userId={}.productId={}",userId,productId);
            return Result.error(404,"订单不存在");
        }

        Long realUserId = order.getUserId();

        if(!userId.equals(realUserId)){
            log.warn("发布评价失败:不是自己的订单,userId={}.productId={}",userId,productId);
            return Result.error(403,"无权限");
        }

        //订单状态校验
        Integer status = order.getStatus();
        if(!status.equals(ORDER_STATUS_COMPLETED)){
            log.warn("发布评价失败:订单未完成,userId={}.productId={}",userId,productId);
            return Result.error(400,"订单未完成");
        }

        //重复评价校验
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getUserId,userId)
                .eq(Review::getProductId,productId)
                .eq(Review::getOrderId,orderId);

        Review r = reviewMapper.selectOne(wrapper);

        if(r != null){
            log.warn("发布评价失败:重复评价,userId={}.productId={}",userId,productId);
            return Result.error(400,"重复评价");
        }

        //商品匹配校验
        LambdaQueryWrapper<OrderItem> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(OrderItem::getProductId,productId)
                .eq(OrderItem::getOrderId,orderId);

        OrderItem orderItem = orderItemMapper.selectOne(wrapper1);

        if(orderItem == null){
            log.warn("发布评价失败:订单与商品不匹配,userId={}.productId={}",userId,productId);
            return Result.error(400,"订单与商品不匹配");
        }

        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(userId);
        review.setContent(content);
        review.setRating(rating);
        review.setOrderId(orderId);

        reviewMapper.insert(review);

        order.setIsReview(ORDER_REVIEW_YES);
        orderMapper.updateById(order);

        log.info("发布评价成功:userId={},productId={}",userId,productId);
        return Result.success();
    }

    //OK
    //删除评价
    @Override
    public Result<Void> deleteReview(Long userId, Long reviewId) {
        log.info("删除评价请求:userId={},reviewId={}",userId,reviewId);

        if(reviewId == null){
            log.warn("删除评价失败:评价不存在,userId={},reviewId={}",userId,reviewId);
            return Result.error(404,"评价不存在");
        }

        //鉴权
        Review review = reviewMapper.selectById(reviewId);
        if(!userId.equals(review.getUserId())){
            log.warn("删除评价失败:无权限,userId={},reviewId={}",userId,reviewId);
            return Result.error(403,"无权限");
        }

        reviewMapper.deleteById(reviewId);

        //更改订单状态
        String orderId = review.getOrderId();
        Order order = orderMapper.selectById(orderId);
        order.setIsReview(ORDER_REVIEW_NO);
        orderMapper.updateById(order);

        log.info("删除评价成功:userId={},reviewId={}",userId,reviewId);
        return Result.success();
    }

    //OK
    //商家回复
    @Override
    public Result<Void> replyReview(Long userId, ReplyReviewRequest request) {
        Long reviewId = request.getReviewId();
        String replyContent = request.getReplyContent();

        log.info("商家回复请求:userId={},reviewId={}",userId,reviewId);

        if(reviewId == null){
            log.warn("商家回复失败:评价不存在,userId={},reviewId={}",userId,reviewId);
            return Result.error(404,"评价不存在");
        }

        //鉴权
        Review review = reviewMapper.selectById(reviewId);
        Long productId = review.getProductId();
        Product product = getById(productId);
        if(product == null){
            log.warn("商家回复失败:商品不存在,userId={},reviewId={}",userId,reviewId);
            return Result.error(404,"商品不存在");
        }

        if(!userId.equals(product.getUserId())){
            log.warn("商家回复失败:无权限,userId={},reviewId={}",userId,reviewId);
            return Result.error(403,"无权限");
        }

        review.setReplyContent(replyContent);
        review.setReplyTime(LocalDateTime.now());
        reviewMapper.updateById(review);

        log.info("商家回复成功:userId={},reviewId={}",userId,reviewId);
        return Result.success();
    }

    //OK
    //显示评价列表
    @Override
    public Result<Page<Review>> showReviewList(Long userId, Long productId, PageRequest pageRequest) {
        log.info("展示评价列表:productId={}",productId);

        Integer pageNum = pageRequest.getPageNum();
        Integer pageSize = pageRequest.getPageSize();

        Page<Review> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId,productId)
                .orderByDesc(Review::getCreateTime);

        reviewMapper.selectPage(page,wrapper);

        log.info("展示评价成功:productId={}",productId);
        return Result.success(page);
    }
}
