package com.powernobug.mall.product.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.product.converter.dto.PlatformAttributeInfoConverter;
import com.powernobug.mall.product.converter.param.PlatformAttributeInfoParamConverter;
import com.powernobug.mall.product.dto.PlatformAttributeInfoDTO;
import com.powernobug.mall.product.dto.PlatformAttributeValueDTO;
import com.powernobug.mall.product.mapper.PlatformAttrInfoMapper;
import com.powernobug.mall.product.mapper.PlatformAttrValueMapper;
import com.powernobug.mall.product.model.PlatformAttributeInfo;
import com.powernobug.mall.product.model.PlatformAttributeValue;
import com.powernobug.mall.product.query.PlatformAttributeParam;
import com.powernobug.mall.product.service.PlatformAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.service.impl
 * @author: HuangWeiLong
 * @date: 2024/9/20 21:03
 */
@Service
public class PlatformAttributeServiceImpl implements PlatformAttributeService {
    @Autowired
    PlatformAttrInfoMapper platformAttrInfoMapper;
    @Autowired
    PlatformAttributeInfoConverter platformAttributeInfoConverter;
    @Autowired
    PlatformAttributeInfoParamConverter platformAttributeInfoParamConverter;
    @Autowired
    PlatformAttrValueMapper platformAttrValueMapper;
    @Override
    public List<PlatformAttributeInfoDTO> getPlatformAttrInfoList(Long firstLevelCategoryId, Long secondLevelCategoryId, Long thirdLevelCategoryId) {
        List<PlatformAttributeInfo> platformAttributeInfos = platformAttrInfoMapper.selectPlatFormAttrInfoList(firstLevelCategoryId, secondLevelCategoryId, thirdLevelCategoryId);
        return platformAttributeInfoConverter.platformAttributeInfoPOs2DTOs(platformAttributeInfos);
    }

    @Override
    public void savePlatformAttrInfo(PlatformAttributeParam platformAttributeParam) {
        PlatformAttributeInfo platformAttributeInfo = platformAttributeInfoParamConverter.attributeInfoParam2Info(platformAttributeParam);
        // 判断平台属性
        if (platformAttributeInfo.getId() != null) {
            // 修改数据
            platformAttrInfoMapper.updateById(platformAttributeInfo);
        } else {
            // 新增
            platformAttrInfoMapper.insert(platformAttributeInfo);
        }

        // platformAttrValue平台属性值，先删除，在新增的方式！
        LambdaQueryWrapper<PlatformAttributeValue> platformAttributeValueQueryWrapper = new LambdaQueryWrapper<>();
        // 删除平台属性原本在数据库中对应的属性值
        platformAttributeValueQueryWrapper.eq(PlatformAttributeValue::getAttrId, platformAttributeInfo.getId());
        platformAttrValueMapper.delete(platformAttributeValueQueryWrapper);

        // 获取页面传递过来的所有平台属性值数据
        List<PlatformAttributeValue> attrValueList = platformAttributeInfo.getAttrValueList();
        if (!CollectionUtils.isEmpty(attrValueList)) {
            // 循环遍历
            for (PlatformAttributeValue platformAttributeValue : attrValueList) {
                platformAttributeValue.setId(null);
                // 获取平台属性Id 给attrId
                platformAttributeValue.setAttrId(platformAttributeInfo.getId());
                platformAttrValueMapper.insert(platformAttributeValue);
            }
        }
    }

    @Override
    public List<PlatformAttributeValueDTO> getPlatformAttrInfo(Long attrId) {
        LambdaQueryWrapper<PlatformAttributeValue> platformAttributeValueLambdaQueryWrapper = new LambdaQueryWrapper<>();
        platformAttributeValueLambdaQueryWrapper.eq(PlatformAttributeValue::getAttrId,attrId);
        List<PlatformAttributeValue> platformAttributeValues = platformAttrValueMapper.selectList(platformAttributeValueLambdaQueryWrapper);
        return platformAttributeInfoConverter.platformAttributeValuePOs2DTO(platformAttributeValues);
    }
}
