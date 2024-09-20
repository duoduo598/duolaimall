package com.powernobug.mall.product.converter.dto;

import com.powernobug.mall.product.dto.PlatformAttributeInfoDTO;
import com.powernobug.mall.product.dto.PlatformAttributeValueDTO;
import com.powernobug.mall.product.model.PlatformAttributeInfo;
import com.powernobug.mall.product.model.PlatformAttributeValue;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlatformAttributeInfoConverter {

    PlatformAttributeInfoDTO platformAttributeInfoPO2DTO(PlatformAttributeInfo platformAttributeInfo);
    List<PlatformAttributeInfoDTO> platformAttributeInfoPOs2DTOs(List<PlatformAttributeInfo> platformAttributeInfos);

    PlatformAttributeValueDTO platformAttributeValuePO2DTO(PlatformAttributeValue platformAttributeValue);
}
