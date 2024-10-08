package com.powernobug.mall.product.controller.inner;

import com.powernobug.mall.product.dto.CategoryHierarchyDTO;
import com.powernobug.mall.product.dto.PlatformAttributeInfoDTO;
import com.powernobug.mall.product.dto.SkuInfoDTO;
import com.powernobug.mall.product.dto.TrademarkDTO;
import com.powernobug.mall.product.service.CategoryService;
import com.powernobug.mall.product.service.PlatformAttributeService;
import com.powernobug.mall.product.service.SkuService;
import com.powernobug.mall.product.service.TrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.controller.inner
 * @author: HuangWeiLong
 * @date: 2024/10/6 17:01
 */
@RestController
public class ProductApiConntroller {
    @Autowired
    SkuService skuService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    TrademarkService trademarkService;
    /**
     * 根据skuId获取sku信息
     *
     * @param skuId
     * @return
     */
    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    public SkuInfoDTO getSkuInfo(@PathVariable("skuId") Long skuId) {

        SkuInfoDTO skuInfoDTO = skuService.getSkuInfo(skuId);
        return skuInfoDTO;
    }


    /**
     * 通过三级分类id查询分类信息
     * @param 、、category3Id
     * @return
     */
    @GetMapping("/api/product/inner/getCategoryView/{thirdLevelCategoryId}")
    public CategoryHierarchyDTO getCategoryView(@PathVariable("thirdLevelCategoryId")Long thirdLevelCategoryId) {
        CategoryHierarchyDTO hierarchyDTO = categoryService.getCategoryViewByCategoryId(thirdLevelCategoryId);
        return hierarchyDTO;

    }


    /**
     * 通过skuId 集合来查询平台属性列表
     * @param skuId
     */
    @GetMapping("/api/product/inner/getAttrList/{skuId}")
    public List<PlatformAttributeInfoDTO> getAttrList(@PathVariable("skuId") Long skuId) {
        List<PlatformAttributeInfoDTO> platformAttrInfoBySku = skuService.getPlatformAttrInfoBySku(skuId);
        return platformAttrInfoBySku;

    }


    /**
     * 通过品牌Id 集合来查询数据
     * @param tmId
     * @return
     */
    @GetMapping("/api/product/inner/getTrademark/{tmId}")
    public TrademarkDTO getTrademark(@PathVariable("tmId")Long tmId) {
        TrademarkDTO trademarkByTmId = trademarkService.getTrademarkByTmId(tmId);
        return trademarkByTmId;

    }
}
