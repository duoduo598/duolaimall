package com.powernobug.mall.search.client;

import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.product.dto.CategoryHierarchyDTO;
import com.powernobug.mall.product.dto.PlatformAttributeInfoDTO;
import com.powernobug.mall.product.dto.SkuInfoDTO;
import com.powernobug.mall.product.dto.TrademarkDTO;
import com.powernobug.mall.search.param.SearchParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "service-product")
public interface ProductApiClient {

    /**
     * 根据skuId获取sku信息
     *
     * @param skuId
     * @return
     */
    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    SkuInfoDTO getSkuInfo(@PathVariable("skuId") Long skuId);


    /**
     * 通过三级分类id查询分类信息
     * @param /category3Id
     * @return
     */
    @GetMapping("/api/product/inner/getCategoryView/{thirdLevelCategoryId}")
    CategoryHierarchyDTO getCategoryView(@PathVariable("thirdLevelCategoryId")Long thirdLevelCategoryId);


    /**
     * 通过skuId 集合来查询数据
     * @param skuId
     */
    @GetMapping("/api/product/inner/getAttrList/{skuId}")
    List<PlatformAttributeInfoDTO> getAttrList(@PathVariable("skuId") Long skuId);


    /**
     * 通过品牌Id 集合来查询数据
     * @param tmId
     * @return
     */
    @GetMapping("/api/product/inner/getTrademark/{tmId}")
    TrademarkDTO getTrademark(@PathVariable("tmId")Long tmId);

}
