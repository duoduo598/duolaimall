package com.powernobug.mall.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.product.dto.SaleAttributeInfoDTO;
import com.powernobug.mall.product.dto.SpuInfoPageDTO;
import com.powernobug.mall.product.model.SpuInfo;
import com.powernobug.mall.product.query.SpuInfoParam;
import com.powernobug.mall.product.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.controller
 * @author: HuangWeiLong
 * @date: 2024/9/21 22:42
 */
@RestController
public class AdminSpuController {
    @Autowired
    SpuService spuService;
    //查询销售属性
    @GetMapping("admin/product/baseSaleAttrList")
    public Result SaleAttrList(){
        List<SaleAttributeInfoDTO> saleAttrInfoList = spuService.getSaleAttrInfoList();
        return Result.ok(saleAttrInfoList);
    }
    //SPU列表的查询
    @GetMapping("admin/product/{page}/{size}")
    public Result getSpuInfoPage(@PathVariable Long page,
                                 @PathVariable Long size,
                                 Long category3Id){
        Page<SpuInfo> pageParam = new Page<>(page,size);
        SpuInfoPageDTO spuInfoPage = spuService.getSpuInfoPage(pageParam, category3Id);
        return Result.ok(spuInfoPage);
    }
    //SPU保存
    @PostMapping("admin/product/saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfoParam spuInfoParam){
        spuService.saveSpuInfo(spuInfoParam);
        return Result.ok(null);
    }
}
