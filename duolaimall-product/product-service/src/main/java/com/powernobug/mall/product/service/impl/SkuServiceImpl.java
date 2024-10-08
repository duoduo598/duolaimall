package com.powernobug.mall.product.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernobug.mall.common.constant.RedisConst;
import com.powernobug.mall.product.cache.RedisCache;
import com.powernobug.mall.product.client.SearchApiClient;
import com.powernobug.mall.product.converter.dto.PlatformAttributeInfoConverter;
import com.powernobug.mall.product.converter.dto.SkuInfoConverter;
import com.powernobug.mall.product.converter.dto.SkuInfoPageConverter;
import com.powernobug.mall.product.converter.dto.SpuInfoConverter;
import com.powernobug.mall.product.converter.param.SkuInfoParamConverter;
import com.powernobug.mall.product.dto.PlatformAttributeInfoDTO;
import com.powernobug.mall.product.dto.SkuInfoDTO;
import com.powernobug.mall.product.dto.SkuInfoPageDTO;
import com.powernobug.mall.product.dto.SpuSaleAttributeInfoDTO;
import com.powernobug.mall.product.mapper.*;
import com.powernobug.mall.product.model.*;
import com.powernobug.mall.product.query.SkuInfoParam;
import com.powernobug.mall.product.service.SkuService;
import com.powernobug.mall.product.service.SpuService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.service.impl
 * @author: HuangWeiLong
 * @date: 2024/9/23 15:26
 */
@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    SpuInfoConverter spuInfoConverter;
    @Autowired
    SkuInfoConverter skuInfoConverter;
    @Autowired
    SkuInfoParamConverter skuInfoParamConverter;
    @Autowired
    SkuInfoPageConverter skuInfoPageConverter;
    @Autowired
    PlatformAttributeInfoConverter platformAttributeInfoConverter;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SpuSaleAttrInfoMapper spuSaleAttrInfoMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    SkuPlatformAttrValueMapper skuPlatformAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    SearchApiClient searchApiClient;
    @Override
    public void saveSkuInfo(SkuInfoParam skuInfoParam) {
              /*
      	 1. 保存SKU基本信息
      	 2. 保存SKU图片
      	 3. 保存销售属性值
      	 4. 保存平台属性值
      */

        // 将sku参数对象，转化为PO对象
        SkuInfo skuInfo = skuInfoParamConverter.SkuInfoParam2Info(skuInfoParam);

        // 保存sku基本信息保存到sku_info
        skuInfoMapper.insert(skuInfo);
        // 获取sku图片列表
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && !skuImageList.isEmpty()) {
            // 循环遍历
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                // 保存sku的多张图片信息, 保存到sku_img
                skuImageMapper.insert(skuImage);
            }
        }

        List<SkuSaleAttributeValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttributeValueList();
        // 调用判断集合方法
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            for (SkuSaleAttributeValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                // 保存sku销售属性值
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }

        List<SkuPlatformAttributeValue> skuAttrValueList = skuInfo.getSkuPlatformAttributeValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            for (SkuPlatformAttributeValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                // 保存sku平台属性值
                skuPlatformAttrValueMapper.insert(skuAttrValue);
            }
        }
    }

    @Override
    public SkuInfoPageDTO getPage(Page<SkuInfo> pageParam) {
        LambdaQueryWrapper<SkuInfo> queryWrapper = new LambdaQueryWrapper<>();
        Page<SkuInfo> skuInfoPage = skuInfoMapper.selectPage(pageParam, queryWrapper);
        return skuInfoPageConverter.skuInfoPagePO2PageDTO(skuInfoPage);
    }

    @Override
    public void onSale(Long skuId) {
        LambdaUpdateWrapper<SkuInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SkuInfo::getId,skuId);
        updateWrapper.set(SkuInfo::getIsSale,1);
        skuInfoMapper.update(null,updateWrapper);


        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        bloomFilter.add(skuId);
        System.out.println("往布隆过滤器中添加了一个元素:" + skuId);

        searchApiClient.upperGoods(skuId);
    }

    @Override
    public void offSale(Long skuId) {
        LambdaUpdateWrapper<SkuInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SkuInfo::getId,skuId);
        updateWrapper.set(SkuInfo::getIsSale,0);
        skuInfoMapper.update(null,updateWrapper);

        searchApiClient.lowerGoods(skuId);
    }
    @RedisCache(prefix = "product:detail:skuInfo:")
    @Override
    public SkuInfoDTO getSkuInfo(Long skuId) {
        // //1.先查redis，查到就返回
        // String key="sku:info:"+skuId;
        // RBucket<SkuInfoDTO> bucket = redissonClient.getBucket(key);
        // SkuInfoDTO skuInfoDTO = bucket.get();
        // if(skuInfoDTO!=null){
        //     return skuInfoDTO;
        // }
        // //2.没找到，查数据库
        // String lockKey=key+":lock";
        // RLock lock = redissonClient.getLock(lockKey);
        //
        // try {
        //     //缓存击穿：加锁
        //     lock.lock();
        //
        //     //double check
        //     skuInfoDTO = bucket.get();
        //     if(skuInfoDTO!=null){
        //         return skuInfoDTO;
        //     }

        //查询数据库
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if(skuInfo==null){
            return new SkuInfoDTO();
        }
        skuInfo.setSkuImageList(skuImageMapper.getSkuImages(skuId));
        return skuInfoConverter.skuInfoPO2DTO(skuInfo);
        //     //缓存穿透：数据库为空，new一个空对象保存在Redis中
        //     if(skuInfoDTO==null){
        //         skuInfoDTO=new SkuInfoDTO();
        //     }
        //
        //     //把数据存入Redis
        //     //缓存雪崩：设置一个过期时间
        //     Random random = new Random();
        //     int randomTime = random.nextInt(60);
        //     bucket.set(skuInfoDTO,120+randomTime, TimeUnit.SECONDS);
        //
        // } finally {
        //    lock.unlock();
        // }
        //
        // //3.返回结果
        // return skuInfoDTO;
    }
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        LambdaQueryWrapper<SkuInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuInfo::getId,skuId);
        SkuInfo skuInfo = skuInfoMapper.selectOne(queryWrapper);
        return skuInfo==null?null:skuInfo.getPrice();
    }
    @RedisCache(prefix = "product:detail:spuSaleAttrCheck:")
    @Override
    public List<SpuSaleAttributeInfoDTO> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        List<SpuSaleAttributeInfo> spuSaleAttributeInfo=spuSaleAttrInfoMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
        return spuInfoConverter.spuSaleAttributeInfoPOs2DTOs(spuSaleAttributeInfo);
    }

    @RedisCache(prefix = "product:detail:platform:")
    @Override
    public List<PlatformAttributeInfoDTO> getPlatformAttrInfoBySku(Long skuId) {
        List<PlatformAttributeInfo> platformAttributeInfos = skuPlatformAttrValueMapper.selectPlatformAttrInfoBySku(skuId);
        return platformAttributeInfoConverter.platformAttributeInfoPOs2DTOs(platformAttributeInfos);
    }

    @Override
    public List<Long> findAllOnSaleProducts() {
        LambdaQueryWrapper<SkuInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SkuInfo::getIsSale, 1);

        List<SkuInfo> skuInfos = skuInfoMapper.selectList(lambdaQueryWrapper);

        // 转化为id的集合
        List<Long> skuIds = skuInfos.stream().map(skuInfo -> skuInfo.getId()).collect(Collectors.toList());

        return skuIds;
    }
}
