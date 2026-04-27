package com.lingfeng.secondhandtradingplatform.controller;


import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import com.lingfeng.secondhandtradingplatform.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    //发布商品
    @PostMapping("/product/publishProduct")
    public Result publishProduct(@RequestHeader("token") String token, @RequestBody Product product){
        return productService.publishProduct(token,product);
    }

    //查询商品详情
    @GetMapping("/product/detail/{productId}")
    public Result productDetail(@PathVariable String productId){
        return productService.productDetail(productId);
    }

    //编辑商品信息
    @PostMapping("/product/update/{productId}")
    public Result productUpdate(@RequestBody Product product
                                ,@PathVariable String productId
                                ,@RequestHeader("token") String token){
        return productService.productUpdate(product,productId,token);
    }

    //下架商品
    @PostMapping("/product/remove/{productId}")
    public Result removeProduct(@PathVariable String productId,@RequestHeader("token") String token){
        return productService.removeProduct(productId,token);
    }

    //删除商品
    @PostMapping("/product/delete/{productId}")
    public Result deleteProduct(@PathVariable String productId,@RequestHeader("token") String token){
        return productService.deleteProduct(productId,token);
    }

    //将已下架的商品重新上架
    @PostMapping("/product/republish/{productId}")
    public Result republishProduct(@PathVariable String productId,@RequestHeader("token") String token){
        return productService.republishProduct(productId,token);
    }

    //我的发布商品
    @GetMapping("/product/myList")
    public Result showMyList(@RequestHeader("token") String token,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        return productService.showMyList(token,pageNum,pageSize);
    }

    //根据搜索展示商品列表






}
