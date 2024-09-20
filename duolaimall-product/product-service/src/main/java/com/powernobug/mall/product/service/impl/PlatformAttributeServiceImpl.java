package com.powernobug.mall.product.service.impl;

import com.powernobug.mall.product.converter.dto.PlatformAttributeInfoConverter;
import com.powernobug.mall.product.dto.PlatformAttributeInfoDTO;
import com.powernobug.mall.product.dto.PlatformAttributeValueDTO;
import com.powernobug.mall.product.mapper.PlatformAttrInfoMapper;
import com.powernobug.mall.product.model.PlatformAttributeInfo;
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
    @Override
    public List<PlatformAttributeInfoDTO> getPlatformAttrInfoList(Long firstLevelCategoryId, Long secondLevelCategoryId, Long thirdLevelCategoryId) {
        List<PlatformAttributeInfo> platformAttributeInfos = platformAttrInfoMapper.selectPlatFormAttrInfoList(firstLevelCategoryId, secondLevelCategoryId, thirdLevelCategoryId);
        return platformAttributeInfoConverter.platformAttributeInfoPOs2DTOs(platformAttributeInfos);
    }

    @Override
    public void savePlatformAttrInfo(PlatformAttributeParam platformAttributeParam) {

    }

    @Override
    public PlatformAttributeValueDTO getPlatformAttrInfo(Long attrId) {
        return null;
    }
}
