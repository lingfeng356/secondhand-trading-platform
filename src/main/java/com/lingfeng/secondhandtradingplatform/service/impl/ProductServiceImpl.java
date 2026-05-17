package com.lingfeng.secondhandtradingplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingfeng.secondhandtradingplatform.DTO.request.*;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.response.ProductDetailResponse;
import com.lingfeng.secondhandtradingplatform.converter.ProductConverter;
import com.lingfeng.secondhandtradingplatform.mapper.ProductMapper;
import com.lingfeng.secondhandtradingplatform.mapper.UserMapper;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import com.lingfeng.secondhandtradingplatform.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.lingfeng.secondhandtradingplatform.constant.RedisConstant.PRODUCT_KEY;
import static com.lingfeng.secondhandtradingplatform.constant.RedisConstant.PRODUCT_TTL;

@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper,Product> implements ProductService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${upload.path}")
    private String uploadPath; // 例如: /uploads/avatars/


    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductConverter productConverter;

    //发布商品
    @Override
    public Result<Void> publishProduct(Long userId, ProductPublishRequest ppr) {

        log.info("发布商品请求:userId={}",userId);

        //将DTO转换成entity
        Product product = productConverter.toEntity(ppr);

        //存数据库
        product.setStatus(1);
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
        stringRedisTemplate.opsForHash().putAll(key,productMap);
        stringRedisTemplate.expire(key,PRODUCT_TTL, TimeUnit.MINUTES);

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
            ProductDetailResponse pdr = productConverter.toProductDetailResponse(product);

            // 浏览量+1（处理 null 的情况）
            int currentViewCount = pdr.getViewCount() == null ? 0 : pdr.getViewCount();
            int newViewCount = currentViewCount + 1;
            pdr.setViewCount(newViewCount);

            //查询user，为注入卖家信息准备
            User user = userMapper.selectById(product.getUserId());
            pdr.setSellerName(user.getUsername());
            pdr.setSellerImg(user.getImg());

            // 更新数据库
            product.setViewCount(newViewCount);
            updateById(product);

            // 更新Redis中的浏览量
            stringRedisTemplate.opsForHash().put(key, "viewCount", String.valueOf(newViewCount));
            stringRedisTemplate.expire(key, PRODUCT_TTL, TimeUnit.MINUTES);

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

        //封装响应DTO
        ProductDetailResponse pdr = productConverter.toProductDetailResponse(product);

        // 浏览量+1（处理 null 的情况）
        int currentViewCount = product.getViewCount() == null ? 0 : product.getViewCount();
        int newViewCount = currentViewCount + 1;
        pdr.setViewCount(newViewCount);

        //TODO:定时任务更新数据库，否则浏览一次就更新一次容易崩
        //查询user，为注入卖家信息准备
        User user = userMapper.selectById(product.getUserId());
        pdr.setSellerName(user.getUsername());
        pdr.setSellerImg(user.getImg());

        //更新数据库
        product.setViewCount(newViewCount);
        updateById(product);

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

        //删除redis缓存
        stringRedisTemplate.delete(PRODUCT_KEY + productId);

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

        if(product.getStatus() != 1){
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
        product.setStatus(3);

        //将修改后的商品保存到数据库
        updateById(product);

        //删除redis缓存
        stringRedisTemplate.delete(PRODUCT_KEY + productId);

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

        //删除redis缓存
        stringRedisTemplate.delete(PRODUCT_KEY + productId);

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

        if(product.getStatus() != 3){
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
        product.setStatus(1);

        //将修改后的商品保存到数据库
        updateById(product);

        //删除redis缓存
        stringRedisTemplate.delete(PRODUCT_KEY + productId);

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
        wrapper.eq(Product::getStatus,1);

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
    //TODO：添加缓存降低DB压力
    @Override
    public Result<IPage<Product>> recommendProducts(PageRequest pageRequest) {

        log.info("推荐首页商品请求");

        Integer pageNum = pageRequest.getPageNum();
        Integer pageSize = pageRequest.getPageSize();

        Page<Product> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus,1)
                .orderByDesc(Product::getViewCount)   // 浏览量
                .orderByDesc(Product::getLikeCount)   // 点赞量
                .orderByDesc(Product::getCreateTime); // 最新

        page(page,wrapper);

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

}
