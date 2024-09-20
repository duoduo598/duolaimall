package com.powernobug.mall.product.converter.dto;

import com.powernobug.mall.product.dto.TrademarkDTO;
import com.powernobug.mall.product.model.Trademark;
import com.powernobug.mall.product.query.TrademarkParam;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrademarkConverter {

    TrademarkDTO trademarkPO2DTO(Trademark trademark);

    List<TrademarkDTO> trademarkPOs2DTOs(List<Trademark> trademarks);

    Trademark trademarkParam2Trademark(TrademarkParam trademarkParam);

}
