package com.powernobug.mall.product.converter.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.powernobug.mall.product.dto.TrademarkPageDTO;

import com.powernobug.mall.product.model.Trademark;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = TrademarkConverter.class)
public interface TrademarkPageConverter {

    TrademarkPageDTO tradeMarkPagePO2PageDTO(IPage<Trademark> trademarkPage);
}
