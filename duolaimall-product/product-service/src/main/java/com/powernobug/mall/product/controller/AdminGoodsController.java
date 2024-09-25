package com.powernobug.mall.product.controller;

import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.product.dto.ProductDetailDTO;
import com.powernobug.mall.product.service.ProductDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.controller
 * @author: HuangWeiLong
 * @date: 2024/9/23 20:05
 */
@RestController
public class AdminGoodsController {
    @Autowired
    ProductDetailService productDetailService;
    @GetMapping("goods/{skuId}")
    public Result<ProductDetailDTO> getItem(@PathVariable Long skuId){
        ProductDetailDTO itemBySkuId = productDetailService.getItemBySkuId(skuId);
        return Result.ok(itemBySkuId);
    }
}
