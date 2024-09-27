package com.powernobug.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.powernobug.mall.common.constant.RedisConst;
import com.powernobug.mall.product.converter.dto.CategoryConverter;
import com.powernobug.mall.product.dto.*;
import com.powernobug.mall.product.mapper.CategoryHierarchyMapper;
import com.powernobug.mall.product.model.CategoryHierarchy;
import com.powernobug.mall.product.service.CategoryService;
import com.powernobug.mall.product.service.ProductDetailService;
import com.powernobug.mall.product.service.SkuService;
import com.powernobug.mall.product.service.SpuService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.service.impl
 * @author: HuangWeiLong
 * @date: 2024/9/23 20:24
 */
@Service
@Slf4j
public class ProductDetailServiceImpl implements ProductDetailService {
    @Autowired
    SkuService skuService;
    @Autowired
    SpuService spuService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    CategoryConverter categoryConverter;
    @Autowired
    RedissonClient redissonClient;
    static ExecutorService executorService= Executors.newFixedThreadPool(10);
    @Override
    public ProductDetailDTO getItemBySkuId(Long skuId) {
        ProductDetailDTO productDetailDTO = new ProductDetailDTO();
        long startTime = System.currentTimeMillis();

        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        if(!bloomFilter.contains(skuId)){
            System.out.println("布隆过滤器对商品详情页的请求进行了拦截，商品id:" + skuId);
            return productDetailDTO;
        }

        log.info("布隆过滤器放行了， 商品id:{}",skuId);

        // 商品sku 信息
        CompletableFuture<SkuInfoDTO> cf1 = CompletableFuture.supplyAsync(() -> {
            SkuInfoDTO skuInfo = skuService.getSkuInfo(skuId);
            productDetailDTO.setSkuInfo(skuInfo);
            return skuInfo;
        }, executorService);


        // 获取指定的sku完整的销售属性值
        CompletableFuture<Void> cf2 = cf1.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttributeInfoDTO> spuSaleAttrListCheckBySku = skuService.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            productDetailDTO.setSpuSaleAttrList(spuSaleAttrListCheckBySku);
        }, executorService);


        // 获取spu中包含的所有的不同销售属性取值的组合
        CompletableFuture<Void> cf3 = cf1.thenAcceptAsync(skuInfo -> {
            Map<String, Long> valueIdsMap = spuService.getSkuValueIdsMap(skuInfo.getSpuId());
            String jsonString = JSON.toJSONString(valueIdsMap);
            productDetailDTO.setValuesSkuJson(jsonString);
        }, executorService);



        // 获取sku商品的价格
        CompletableFuture<Void> cf4 = CompletableFuture.runAsync(() -> {
            BigDecimal skuPrice = skuService.getSkuPrice(skuId);
            productDetailDTO.setPrice(skuPrice);
        }, executorService);


        //获取三级类目的完整类目视图
        CompletableFuture<Void> cf5 = cf1.thenAcceptAsync(skuInfo -> {
            CategoryHierarchyDTO categoryViewByCategoryId = categoryService.getCategoryViewByCategoryId(skuInfo.getThirdLevelCategoryId());
            productDetailDTO.setCategoryHierarchy(categoryViewByCategoryId);
        }, executorService);


        //获取sku商品的海报列表
        CompletableFuture<Void> cf6 = cf1.thenAcceptAsync(skuInfo -> {
            List<SpuPosterDTO> spuPosterBySpuId = spuService.findSpuPosterBySpuId(skuInfo.getSpuId());
            productDetailDTO.setSpuPosterList(spuPosterBySpuId);
        }, executorService);


        //获取sku商品对应的平台属性集合(规格参数)
        CompletableFuture<Void> cf7 = CompletableFuture.runAsync(() -> {
            SkuSpecification skuSpecification = new SkuSpecification();
            List<PlatformAttributeInfoDTO> platformAttributeInfoDTOs = skuService.getPlatformAttrInfoBySku(skuId);
            List<SkuSpecification> skuSpecifications = platformAttributeInfoDTOs.stream().map(platformAttrInfoBySku -> {
                        String attrName = platformAttrInfoBySku.getAttrName();
                        skuSpecification.setAttrName(attrName);
                        List<PlatformAttributeValueDTO> attrValueList = platformAttrInfoBySku.getAttrValueList();
                        if (!CollectionUtils.isEmpty(attrValueList)) {
                            String valueName = attrValueList.get(0).getValueName();
                            skuSpecification.setAttrValue(valueName);
                        }
                        return skuSpecification;
                    }
            ).toList();
            productDetailDTO.setSkuAttrList(skuSpecifications);
        }, executorService);

        // 等待所有的异步任务都执行结束，再返回
        CompletableFuture.allOf(cf2,cf3,cf4,cf5,cf6,cf7).join();

        long endTime = System.currentTimeMillis();
        System.out.println("查询的总耗时为："+(endTime-startTime)+"毫秒");
        //返回结果
        return productDetailDTO;
    }
}
