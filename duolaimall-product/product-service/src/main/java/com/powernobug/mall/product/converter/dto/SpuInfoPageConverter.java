package com.powernobug.mall.product.converter.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernobug.mall.product.dto.SpuInfoPageDTO;
import com.powernobug.mall.product.model.SpuInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = SpuInfoConverter.class)
public interface SpuInfoPageConverter {

    SpuInfoPageDTO spuInfoPage2PageDTO(Page<SpuInfo> SpuInfoPage);

}
