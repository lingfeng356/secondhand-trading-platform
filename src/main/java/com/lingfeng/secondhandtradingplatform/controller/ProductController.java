package com.lingfeng.secondhandtradingplatform.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingfeng.secondhandtradingplatform.DTO.request.*;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.response.ProductDetailResponse;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import com.lingfeng.secondhandtradingplatform.pojo.Review;
import com.lingfeng.secondhandtradingplatform.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "商品模块")
@RequestMapping("/product")
@Validated  // 加在类上，类中所有方法参数都会自动校验
public class ProductController {

    @Autowired
    private ProductService productService;

    //发布商品
    @PostMapping("/publishProduct")
    @Operation(summary = "发布商品")
    @SaCheckLogin
    public Result<Void> publishProduct(@RequestBody ProductPublishRequest ppr){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.publishProduct(userId,ppr);
    }

    //查询商品详情
    @GetMapping("/detail/{productId}")
    @Operation(summary = "查询商品详情")
    @ApiResponse(responseCode = "404",description = "商品不存在")
    public Result<ProductDetailResponse> productDetail(@PathVariable Long productId){
        return productService.productDetail(productId);
    }

    //编辑商品信息
    @PostMapping("/update/{productId}")
    @Operation(summary = "编辑商品信息")
    @ApiResponses({
            @ApiResponse(responseCode = "404",description = "商品id无效"),
            @ApiResponse(responseCode = "403",description = "无修改权限")
    })
    @SaCheckLogin
    public Result<Void> productUpdate(@RequestBody UpdateProductDetailRequest updr
                                , @PathVariable Long productId){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.productUpdate(userId,updr,productId);
    }

    //下架商品
    @PostMapping("/remove/{productId}")
    @Operation(summary = "下架商品")
    @ApiResponses({
            @ApiResponse(responseCode = "404",description = "商品不存在"),
            @ApiResponse(responseCode = "400",description = "商品当前状态无法修改"),
            @ApiResponse(responseCode = "403",description = "无修改权限")
    })
    @SaCheckLogin
    public Result<Void> removeProduct(@PathVariable Long productId){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.removeProduct(userId,productId);
    }

    //删除商品
    @PostMapping("/delete/{productId}")
    @Operation(summary = "删除商品")
    @ApiResponses({
            @ApiResponse(responseCode = "404",description = "商品不存在"),
            @ApiResponse(responseCode = "400",description = "商品当前状态无法修改"),
            @ApiResponse(responseCode = "403",description = "无修改权限")
    })
    @SaCheckLogin
    public Result<Void> deleteProduct(@PathVariable Long productId){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.deleteProduct(userId,productId);
    }

    //将已下架的商品重新上架
    @PostMapping("/republish/{productId}")
    @Operation(summary = "重新上架商品")
    @ApiResponses({
            @ApiResponse(responseCode = "404",description = "商品不存在"),
            @ApiResponse(responseCode = "400",description = "商品当前状态无法修改"),
            @ApiResponse(responseCode = "403",description = "无修改权限")
    })
    @SaCheckLogin
    public Result<Void> republishProduct(@PathVariable Long productId){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.republishProduct(userId,productId);
    }

    //我的发布商品
    @PostMapping("/myList")
    @Operation(summary = "查询我发布的商品")
    @SaCheckLogin
    public Result<IPage<Product>> showMyList(@RequestBody PageRequest pageRequest){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.showMyList(userId,pageRequest);
    }

    //根据搜索展示商品列表,分页，排序，条件查询
    @PostMapping("/list")
    @Operation(summary = "分页条件查询商品")
    public Result<IPage<Product>> showList(@RequestBody ProductListRequest productListRequestDTO){
        return productService.showProductList(productListRequestDTO);
    }

    //首页商品个性化推荐
    @PostMapping("/recommendProducts")
    @Operation(summary = "根据热度推荐首页商品")
    public Result<IPage<Product>> recommendProducts(@RequestBody PageRequest pageRequest){
        return productService.recommendProducts(pageRequest);
    }

    //根据分类展示商品列表
    @PostMapping("/showByCategory")
    @Operation(summary = "分类展示商品")
    public Result<IPage<Product>> showByCategory(@RequestBody ShowProductByCategoryRequest request){
        return productService.showByCategory(request);
    }

    //上传商品图片
    @PostMapping("/upload/{productId}")
    @Operation(summary = "上传商品图片")
    @SaCheckLogin
    public Result<Void> upload(@RequestParam("file") MultipartFile file,@PathVariable Long productId){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.upload(file,userId,productId);
    }

    //点赞商品
    @PostMapping("/like/{productId}")
    @Operation(summary = "点赞商品")
    @SaCheckLogin
    public Result<Void> likeProduct(@PathVariable("productId") Long productId){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.likeProduct(userId,productId);
    }

    //取消点赞
    @PostMapping("/cancelLike/{productId}")
    @Operation(summary = "取消点赞商品")
    @SaCheckLogin
    public Result<Void> cancelLikeProduct(@PathVariable("productId") Long productId){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.cancelLikeProduct(userId,productId);
    }

    //检查是否已点赞
    @GetMapping("/checkIsLike/{productId}")
    @Operation(summary = "检查是否点赞")
    @SaCheckLogin
    public Result<Boolean> checkIsLike(@PathVariable("productId") Long productId){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.checkIsLike(userId,productId);
    }

    //收藏
    @PostMapping("/collectProduct/{productId}")
    @Operation(summary = "收藏商品")
    @SaCheckLogin
    public Result<Void> collectProduct(@PathVariable("productId") Long productId){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.collectProduct(userId,productId);
    }

    //取消收藏
    @PostMapping("/cancelCollect/{productId}")
    @Operation(summary = "取消收藏")
    @SaCheckLogin
    public Result<Void> cancelCollect(@PathVariable("productId") Long productId){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.cancelCollect(userId,productId);
    }

    //是否收藏
    @GetMapping("/checkCollect/{productId}")
    @Operation(summary = "是否收藏")
    @SaCheckLogin
    public Result<Boolean> checkCollect(@PathVariable("productId") Long productId){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.checkIsCollect(userId,productId);
    }

    //我的收藏商品
    @PostMapping("/myCollectProducts")
    @Operation(summary = "我的收藏商品")
    @SaCheckLogin
    public Result<Page<Product>> myCollectProducts(@RequestBody PageRequest pageRequest){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.myCollectProducts(userId,pageRequest);
    }

    //发布评价
    @PostMapping("/publishReview")
    @Operation(summary = "发布评价")
    @SaCheckLogin
    public Result<Void> publishReview(@RequestBody PublishReviewRequest request){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.publishReview(userId,request);
    }

    //删除评价
    @PostMapping("/deleteReview/{reviewId}")
    @Operation(summary = "删除评价")
    @SaCheckLogin
    public Result<Void> deleteReview(@PathVariable("reviewId") Long reviewId){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.deleteReview(userId,reviewId);
    }

    //显示评价列表
    @PostMapping("/showReviewList/{productId}")
    @Operation(summary = "显示评价")
    public Result<Page<Review>> showReviewList(@PathParam("productId") Long productId,
                                               @RequestBody PageRequest pageRequest){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.showReviewList(userId,productId,pageRequest);
    }

    //商家回复
    @PostMapping("/reply")
    @Operation(summary = "商家回复")
    @SaCheckLogin
    public Result<Void> replyReview(@RequestBody ReplyReviewRequest request){
        Long userId = StpUtil.getLoginIdAsLong();
        return productService.replyReview(userId,request);
    }
}
