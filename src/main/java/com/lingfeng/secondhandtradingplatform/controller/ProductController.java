package com.lingfeng.secondhandtradingplatform.controller;


import com.lingfeng.secondhandtradingplatform.DTO.request.ProductListRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.PageRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.ProductPublishRequest;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.UpdateProductDetailRequest;
import com.lingfeng.secondhandtradingplatform.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "商品模块")
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    //发布商品
    @PostMapping("/publishProduct")
    @Operation(summary = "发布商品")
    public Result publishProduct(@RequestBody ProductPublishRequest ppr){
        return productService.publishProduct(ppr);
    }

    //查询商品详情
    @GetMapping("/detail/{productId}")
    @Operation(summary = "查询商品详情")
    @ApiResponse(responseCode = "404",description = "商品不存在")
    public Result productDetail(@PathVariable String productId){
        return productService.productDetail(productId);
    }

    //编辑商品信息
    @PostMapping("/update/{productId}")
    @Operation(summary = "编辑商品信息")
    @ApiResponses({
            @ApiResponse(responseCode = "404",description = "商品id无效"),
            @ApiResponse(responseCode = "403",description = "无修改权限")
    })
    public Result productUpdate(@RequestBody UpdateProductDetailRequest updr
                                , @PathVariable String productId){
        return productService.productUpdate(updr,productId);
    }

    //下架商品
    @PostMapping("/remove/{productId}")
    @Operation(summary = "下架商品")
    @ApiResponses({
            @ApiResponse(responseCode = "404",description = "商品不存在"),
            @ApiResponse(responseCode = "400",description = "商品当前状态无法修改"),
            @ApiResponse(responseCode = "403",description = "无修改权限")
    })
    public Result removeProduct(@PathVariable String productId){
        return productService.removeProduct(productId);
    }

    //删除商品
    @PostMapping("/delete/{productId}")
    @Operation(summary = "删除商品")
    @ApiResponses({
            @ApiResponse(responseCode = "404",description = "商品不存在"),
            @ApiResponse(responseCode = "400",description = "商品当前状态无法修改"),
            @ApiResponse(responseCode = "403",description = "无修改权限")
    })
    public Result deleteProduct(@PathVariable String productId){
        return productService.deleteProduct(productId);
    }

    //将已下架的商品重新上架
    @PostMapping("/republish/{productId}")
    @Operation(summary = "重新上架商品")
    @ApiResponses({
            @ApiResponse(responseCode = "404",description = "商品不存在"),
            @ApiResponse(responseCode = "400",description = "商品当前状态无法修改"),
            @ApiResponse(responseCode = "403",description = "无修改权限")
    })
    public Result republishProduct(@PathVariable String productId){
        return productService.republishProduct(productId);
    }

    //我的发布商品
    @PostMapping("/myList")
    @Operation(summary = "查询我发布的商品")
    public Result showMyList(@RequestBody PageRequest pageRequest){
        return productService.showMyList(pageRequest);
    }

    //根据搜索展示商品列表,分页，排序，条件查询
    @PostMapping("/list")
    @Operation(summary = "分页条件查询商品")
    public Result showList(@RequestBody ProductListRequest productListRequestDTO){
        return productService.showMyList(productListRequestDTO);
    }

    //首页商品个性化推荐
    @PostMapping("/recommendProducts")
    @Operation(summary = "根据热度推荐首页商品")
    public Result recommendProducts(@RequestBody PageRequest pageRequest){
        return productService.recommendProducts(pageRequest);
    }
}
