package com.lingfeng.secondhandtradingplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingfeng.secondhandtradingplatform.DTO.ProductListRequestDTO;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.mapper.ProductMapper;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import com.lingfeng.secondhandtradingplatform.service.ProductService;
import com.lingfeng.secondhandtradingplatform.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private UserUtils userUtils;

    //发布商品
    @Override
    public Result publishProduct(String token, Product product) {
        //token校验
        Long userId = UserUtils.getIdByToken(token);
        log.info("查询到用户id为{}",userId);
        //userid在过滤器中判断是否为null
        //商品参数校验在controller类和product类中注解进行校验
        //存数据库
        product.setStatus(1);
        product.setUserId(userId);
        log.info("保存用户id为{}",userId);
        save(product);
        log.info("已保存到数据库");
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
        log.info("已保存到redis");
        //返回结果

        return Result.success();
    }

    //查看商品详情
    @Override
    public Result productDetail(String productId) {
        //校验id
        if(productId == null || productId.trim().isEmpty()){
            log.info("商品id不存在");
            return Result.error("商品id为空");
        }

        //从redis查
        String key = PRODUCT_KEY + productId;
        log.info("查询到该商品的tokenKey为{}",key);
        //将对象从hashmap转成json再转成product对象
        Map<Object,Object> tmpMap = stringRedisTemplate.opsForHash().entries(key);

        //如果不为null,直接返回结果
        if (!tmpMap.isEmpty()) {
            String json = JSONUtil.toJsonStr(tmpMap);
            Product product = JSONUtil.toBean(json, Product.class);

            // 浏览量+1（处理 null 的情况）
            int currentViewCount = product.getViewCount() == null ? 0 : product.getViewCount();
            int newViewCount = currentViewCount + 1;
            product.setViewCount(newViewCount);

            // 更新数据库
            updateById(product);

            // 更新Redis中的浏览量
            stringRedisTemplate.opsForHash().put(key, "viewCount", String.valueOf(newViewCount));
            stringRedisTemplate.expire(key, PRODUCT_TTL, TimeUnit.MINUTES);

            log.info("成功获取商品信息，浏览量:{}", newViewCount);
            return Result.success(product);
        }

        //没有则去数据库查
        log.info("redis不存在商品信息，正在从数据库中查询");
        Product product = productMapper.selectById(productId);

        //如果为null则说明数据库没有该商品数据
        if(product == null){
            log.info("商品不存在");
            return Result.error("商品不存在");
        }

        // 浏览量+1（处理 null 的情况）
        int currentViewCount = product.getViewCount() == null ? 0 : product.getViewCount();
        int newViewCount = currentViewCount + 1;
        product.setViewCount(newViewCount);

        //缓存redis
        //将product对象转成map再将内部所有属性转换成String类型
        log.info("正在将商品缓存到redis中");
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
        log.info("已保存到redis");

        return Result.success(product);
    }

    //编辑商品信息
    @Override
    public Result productUpdate(Product product, String productId,String token) {
        //校验id是否合法
        if(productId == null || productId.trim().isEmpty()){
            log.info("商品id不能为空");
            return Result.error("商品id为空");
        }

        //鉴权token,判断是否有资格编辑商品信息
        Long userId = UserUtils.getIdByToken(token);
        log.info("正在获取用户id,用户id为{}",userId);
        Product pct = getById(productId);
        //判断product是否存在
        if(pct == null){
            return Result.error("商品不存在");
        }
        Long realUserId = pct.getUserId();
        log.info("正在获取商品用户id,id为{}",realUserId);
        if(!realUserId.equals(userId)){
            log.info("不允许修改他人商品信息");
            return Result.error("只能修改自己的商品");
        }

        //校验传入参数是否合法,已经在product类中通过valid校验了
        //更新数据库
        //因为前端传来的product没有id，需要手动传参，到数据库后会自动更改非null的数据
        product.setId(Long.parseLong(productId));
        log.info("正在给product传id");
        updateById(product);
        log.info("更新product成功");

        //删除redis缓存
        stringRedisTemplate.delete(PRODUCT_KEY + productId);
        log.info("删除redis缓存成功");

        return Result.success();
    }

    //下架商品
    @Override
    public Result removeProduct(String productId, String token) {
        //校验id是否合法
        if(productId == null || productId.trim().isEmpty()){
            log.info("商品id不能为空");
            return Result.error("商品id为空");
        }

        //鉴权token,判断是否有资格编辑商品信息
        Long userId = UserUtils.getIdByToken(token);
        log.info("正在获取用户id,用户id为{}",userId);
        Product product = getById(productId);
        //判断product是否存在
        if(product == null){
            return Result.error("商品不存在");
        }
        Long realUserId = product.getUserId();
        log.info("正在获取商品用户id,id为{}",realUserId);
        if(!realUserId.equals(userId)){
            log.info("不允许修改他人商品信息");
            return Result.error("只能修改自己的商品");
        }

        //数据库查询商品
        //不为null则修改status
        product.setStatus(2);
        log.info("正在修改商品状态为下架");
        //将修改后的商品保存到数据库
        boolean flag = updateById(product);
        log.info("正在保存到数据库");
        if(!flag){
            return Result.error("发生错误,更新失败");
        }

        //删除redis缓存
        stringRedisTemplate.delete(PRODUCT_KEY + productId);
        log.info("正在删除缓存");
        //返回结果

        log.info("正在返回结果");
        return Result.success();
    }

    //删除商品
    @Override
    public Result deleteProduct(String productId, String token) {
        //校验id是否合法
        if(productId == null || productId.trim().isEmpty()){
            log.info("商品id不能为空");
            return Result.error("商品id为空");
        }

        //鉴权token,判断是否有资格编辑商品信息
        Long userId = UserUtils.getIdByToken(token);
        log.info("正在获取用户id,用户id为{}",userId);
        Product product = getById(productId);
        //判断product是否存在
        if(product == null){
            return Result.error("商品不存在");
        }
        Long realUserId = product.getUserId();
        log.info("正在获取商品用户id,id为{}",realUserId);
        if(!realUserId.equals(userId)){
            log.info("不允许修改他人商品信息");
            return Result.error("只能修改自己的商品");
        }

        //数据库查询商品
        //不为null则删除
        boolean flag = removeById(productId);
        log.info("正在删除商品数据");
        if(!flag){
            return Result.error("发生错误,删除失败");
        }

        //删除redis缓存
        stringRedisTemplate.delete(PRODUCT_KEY + productId);
        log.info("正在删除缓存");
        //返回结果

        log.info("正在返回结果");
        return Result.success();
    }

    //将已下架的商品重新上架
    @Override
    public Result republishProduct(String productId, String token) {
        //校验id是否合法
        if(productId == null || productId.trim().isEmpty()){
            log.info("商品id不能为空");
            return Result.error("商品id为空");
        }

        //鉴权token,判断是否有资格编辑商品信息
        Long userId = UserUtils.getIdByToken(token);
        log.info("正在获取用户id,用户id为{}",userId);
        Product product = getById(productId);
        //判断product是否存在
        if(product == null){
            return Result.error("商品不存在");
        }
        Long realUserId = product.getUserId();
        log.info("正在获取商品用户id,id为{}",realUserId);
        if(!realUserId.equals(userId)){
            log.info("不允许修改他人商品信息");
            return Result.error("只能修改自己的商品");
        }

        //数据库查询商品
        //不为null则修改status
        product.setStatus(1);
        log.info("正在修改商品状态为上架");
        //将修改后的商品保存到数据库
        boolean flag = updateById(product);
        log.info("正在保存到数据库");
        if(!flag){
            return Result.error("发生错误,更新失败");
        }

        //删除redis缓存
        stringRedisTemplate.delete(PRODUCT_KEY + productId);
        log.info("正在删除缓存");
        //返回结果

        log.info("正在返回结果");
        return Result.success();
    }

    //我的发布商品
    @Override
    public Result showMyList(String token,Integer pageNum,Integer pageSize) {
        //获取userId,因为登录状态已经在拦截器里判断过了，所以不需要判断是否为空
        Long userId = UserUtils.getIdByToken(token);

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
    public Result showMyList(ProductListRequestDTO productListRequestDTO) {
        //构建分页对象
        int pageNum = productListRequestDTO.getPageNum() == null ? 1 : productListRequestDTO.getPageNum();
        int pageSize = productListRequestDTO.getPageSize() == null ? 10 : productListRequestDTO.getPageSize();
        Page<Product> page = new Page<>(pageNum,pageSize);

        //构建查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        //分类筛选
        if(StringUtils.hasText(productListRequestDTO.getCategory())){
            wrapper.eq(Product::getCategory,productListRequestDTO.getCategory());//mybatis-plus会将这个方法引用转变成category字符串
        }

        //价格范围筛选(qs:如果都为null会怎么样子)
        if(productListRequestDTO.getMinPrice() != null){
            wrapper.ge(Product::getPrice,productListRequestDTO.getMinPrice());
        }
        if(productListRequestDTO.getMaxPrice() != null){
            wrapper.le(Product::getPrice,productListRequestDTO.getMaxPrice());
        }

        //城市筛选
        if(StringUtils.hasText(productListRequestDTO.getCity())){
            wrapper.eq(Product::getCity,productListRequestDTO.getCity());
        }

        //搜索栏模糊查询
        if(StringUtils.hasText(productListRequestDTO.getTitle())){
            wrapper.like(Product::getTitle,productListRequestDTO.getTitle());
        }

        //排序
        String sortBy = productListRequestDTO.getSortBy();
        String sortOrder = productListRequestDTO.getSortOrder();

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
        IPage<Product> result = productMapper.selectPage(page,wrapper);

        //返回结果
        return Result.success(result);
    }

    //根据热度推荐首页商品
    @Override
    public Result recommendProducts(Integer pageNum, Integer pageSize) {
        Page<Product> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus,1)
                .orderByDesc(Product::getViewCount)   // 浏览量
                .orderByDesc(Product::getLikeCount)   // 点赞量
                .orderByDesc(Product::getCreateTime); // 最新

        page(page,wrapper);

        return Result.success(page);
    }


}
