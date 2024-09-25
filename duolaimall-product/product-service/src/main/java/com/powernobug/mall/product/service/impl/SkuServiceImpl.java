package com.powernobug.mall.product.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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
    }

    @Override
    public void offSale(Long skuId) {
        LambdaUpdateWrapper<SkuInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SkuInfo::getId,skuId);
        updateWrapper.set(SkuInfo::getIsSale,0);
        skuInfoMapper.update(null,updateWrapper);
    }

    @Override
    public SkuInfoDTO getSkuInfo(Long skuId) {
        LambdaQueryWrapper<SkuInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuInfo::getId,skuId);
        SkuInfo skuInfo = skuInfoMapper.selectOne(queryWrapper);
        //封装sku商品平台属性值集合
        LambdaQueryWrapper<SkuPlatformAttributeValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuPlatformAttributeValue::getSkuId,skuId);
        skuInfo.setSkuPlatformAttributeValueList(skuPlatformAttrValueMapper.selectList(wrapper));
        //封装sku商品图片列表

        skuInfo.setSkuImageList(skuImageMapper.getSkuImages(skuId));

        //封装sku销售属性值集合
        LambdaQueryWrapper<SkuSaleAttributeValue> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(SkuSaleAttributeValue::getSkuId,skuId);
        skuInfo.setSkuSaleAttributeValueList(skuSaleAttrValueMapper.selectList(wrapper2));

        return skuInfoConverter.skuInfoPO2DTO(skuInfo);
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        LambdaQueryWrapper<SkuInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuInfo::getId,skuId);
        SkuInfo skuInfo = skuInfoMapper.selectOne(queryWrapper);
        return skuInfo.getPrice();
    }

    @Override
    public List<SpuSaleAttributeInfoDTO> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        List<SpuSaleAttributeInfo> spuSaleAttributeInfo=spuSaleAttrInfoMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
        return spuInfoConverter.spuSaleAttributeInfoPOs2DTOs(spuSaleAttributeInfo);
    }

    @Override
    public List<PlatformAttributeInfoDTO> getPlatformAttrInfoBySku(Long skuId) {
        List<PlatformAttributeInfo> platformAttributeInfos = skuPlatformAttrValueMapper.selectPlatformAttrInfoBySku(skuId);
        return platformAttributeInfoConverter.platformAttributeInfoPOs2DTOs(platformAttributeInfos);
    }
}
