package com.powernobug.mall.product.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernobug.mall.product.converter.dto.SaleAttributeInfoConverter;
import com.powernobug.mall.product.converter.dto.SpuInfoConverter;
import com.powernobug.mall.product.converter.dto.SpuInfoPageConverter;
import com.powernobug.mall.product.converter.param.SpuInfoParamConverter;
import com.powernobug.mall.product.dto.*;
import com.powernobug.mall.product.mapper.*;
import com.powernobug.mall.product.model.*;
import com.powernobug.mall.product.query.SpuInfoParam;
import com.powernobug.mall.product.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.service.impl
 * @author: HuangWeiLong
 * @date: 2024/9/21 22:50
 */
@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    SaleAttributeInfoConverter saleAttributeInfoConverter;
    @Autowired
    SpuInfoConverter spuInfoConverter;
    @Autowired
    SpuInfoPageConverter spuInfoPageConverter;
    @Autowired
    SpuInfoParamConverter spuInfoParamConverter;
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    SpuImageMapper spuImageMapper;
    @Autowired
    SpuPosterMapper spuPosterMapper;
    @Autowired
    SaleAttrInfoMapper saleAttrInfoMapper;
    @Autowired
    SpuSaleAttrInfoMapper spuSaleAttrInfoMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Override
    public List<SaleAttributeInfoDTO> getSaleAttrInfoList() {
        List<SaleAttributeInfo> saleAttributeInfos = saleAttrInfoMapper.selectList(null);
        return saleAttributeInfoConverter.saleAttributeInfoPOs2DTOs(saleAttributeInfos);
    }

    @Override
    public SpuInfoPageDTO getSpuInfoPage(Page<SpuInfo> pageParam, Long category3Id) {
        LambdaQueryWrapper<SpuInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpuInfo::getThirdLevelCategoryId,category3Id);
        Page<SpuInfo> infoPage = spuInfoMapper.selectPage(pageParam, queryWrapper);
        return spuInfoPageConverter.spuInfoPage2PageDTO(infoPage);
    }

    @Override
    public void saveSpuInfo(SpuInfoParam spuInfoParam) {
        // 现将参数对象转化为PO对象
        SpuInfo spuInfo = spuInfoParamConverter.spuInfoParam2Info(spuInfoParam);
        // 插入基本的spu信息
        spuInfoMapper.insert(spuInfo);

        Long spuId = spuInfo.getId();
        //  获取到spuImage 集合数据
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();

		/*
		   循环遍历spuImageList
		   1. 给spuImage的spuId属性赋值因为只有将spu基本信息插入数据库spuInfo的id才有值
		   2. 保存spuImge
          */
        spuImageList.forEach(spuImage -> {
            spuImage.setSpuId(spuId);
            spuImageMapper.insert(spuImage);
        });

        //  获取销售属性集合
        List<SpuSaleAttributeInfo> spuSaleAttributeInfoList = spuInfo.getSpuSaleAttributeInfoList();

        //  判断
        if (!CollectionUtils.isEmpty(spuSaleAttributeInfoList)) {
            //  循环遍历
            for (SpuSaleAttributeInfo spuSaleAttrInfo : spuSaleAttributeInfoList) {
                /*
                    给spuSaleAttrInfo的spuId赋值，并保存spuSaleAttrInfo到数据库
                    SpuSaleAttrInfoMapper
                */
                spuSaleAttrInfo.setSpuId(spuId);
                spuSaleAttrInfoMapper.insert(spuSaleAttrInfo);
                Long spuSaleAttrId = spuSaleAttrInfo.getId();
                //  再此获取销售属性值集合
                List<SpuSaleAttributeValue> spuSaleAttributeValueList = spuSaleAttrInfo.getSpuSaleAttrValueList();

                /*
                	遍历销售属性值集合spuSaleAttributeValueList
                	1. 给spuSaleAttrValue的spuId赋值
                	2. 给spuSaleAttrValue的spuSaleAttrId赋值
                	3. 保存spuSaleAttrValue到数据库
                */
                spuSaleAttributeValueList.forEach(spuSaleAttributeValue -> {
                    spuSaleAttributeValue.setSpuId(spuId);
                    spuSaleAttributeValue.setSpuSaleAttrId(spuSaleAttrId);
                    spuSaleAttrValueMapper.insert(spuSaleAttributeValue);
                });
            }
        }

        //  获取到posterList 集合数据
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        /*
           遍历销售属性值集合spuPosterList
           1. 给spuPoster的spuId赋值
            2. 保存spuPoster到数据库
         */
        spuPosterList.forEach(spuPoster -> {
            spuPoster.setSpuId(spuId);
            spuPosterMapper.insert(spuPoster);
        });

    }

    @Override
    public List<SpuImageDTO> getSpuImageList(Long spuId) {
        LambdaQueryWrapper<SpuImage> spuImageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spuImageLambdaQueryWrapper.eq(SpuImage::getSpuId,spuId);
        List<SpuImage> spuImages = spuImageMapper.selectList(spuImageLambdaQueryWrapper);
        return spuInfoConverter.spuImagePOs2DTOs(spuImages);
    }

    @Override
    public List<SpuSaleAttributeInfoDTO> getSpuSaleAttrList(Long spuId) {
        List<SpuSaleAttributeInfo> spuSaleAttributeInfos = spuSaleAttrInfoMapper.selectSpuSaleAttrList(spuId);
        return spuInfoConverter.spuSaleAttributeInfoPOs2DTOs(spuSaleAttributeInfos);
    }

    @Override
    public List<SpuPosterDTO> findSpuPosterBySpuId(Long spuId) {
        LambdaQueryWrapper<SpuPoster> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpuPoster::getSpuId,spuId);
        List<SpuPoster> spuPosters = spuPosterMapper.selectList(wrapper);
        return spuInfoConverter.spuPosterPOs2DTOs(spuPosters);
    }

    @Override
    public Map<String, Long> getSkuValueIdsMap(Long spuId) {
        return null;
    }
}
