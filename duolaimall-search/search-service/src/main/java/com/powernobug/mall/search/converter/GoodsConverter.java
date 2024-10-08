package com.powernobug.mall.search.converter;

import com.powernobug.mall.search.dto.GoodsDTO;
import com.powernobug.mall.search.dto.SearchAttrDTO;
import com.powernobug.mall.search.model.Goods;
import com.powernobug.mall.search.model.SearchAttr;
import org.mapstruct.Mapper;


import java.util.List;

@Mapper(componentModel = "spring")
public interface GoodsConverter {

    GoodsDTO goodsPO2DTO(Goods goods);

    List<GoodsDTO> goodsPOs2DTOs(List<Goods> goods);

    SearchAttrDTO searchAttrPO2DTO(SearchAttr searchAttr);
}
