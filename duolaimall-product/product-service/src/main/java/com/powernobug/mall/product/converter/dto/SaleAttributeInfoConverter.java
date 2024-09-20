package com.powernobug.mall.product.converter.dto;

import com.powernobug.mall.product.dto.SaleAttributeInfoDTO;
import com.powernobug.mall.product.model.SaleAttributeInfo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SaleAttributeInfoConverter {

    SaleAttributeInfoDTO saleAttributeInfoPO2DTO(SaleAttributeInfo saleAttributeInfo);
    List<SaleAttributeInfoDTO> saleAttributeInfoPOs2DTOs(List<SaleAttributeInfo> saleAttributeInfos);
}
