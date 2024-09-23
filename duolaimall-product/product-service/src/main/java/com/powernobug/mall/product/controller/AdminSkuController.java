package com.powernobug.mall.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.product.dto.SkuInfoPageDTO;
import com.powernobug.mall.product.model.SkuInfo;
import com.powernobug.mall.product.query.SkuInfoParam;
import com.powernobug.mall.product.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.controller
 * @author: HuangWeiLong
 * @date: 2024/9/23 14:05
 */
@RestController
public class AdminSkuController {
    @Autowired
    SkuService skuService;
    //保存sku
    @PostMapping("admin/product/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfoParam skuInfo) {
        // 调用服务层
        skuService .saveSkuInfo(skuInfo);
        return Result.ok();
    }
    //分页查询sku列表
    @GetMapping("admin/product/list/{page}/{limit}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long limit) {
        Page<SkuInfo> skuInfoPage = new Page<>(page,limit);
        SkuInfoPageDTO pageDTO = skuService.getPage(skuInfoPage);
        return Result.ok(pageDTO);
    }
    //商品上架
    @GetMapping("admin/product/onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId) {
        skuService.onSale(skuId);
        return Result.ok();
    }
    //商品下架
    @GetMapping("admin/product/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId) {
        skuService.offSale(skuId);
        return Result.ok();
    }
}
