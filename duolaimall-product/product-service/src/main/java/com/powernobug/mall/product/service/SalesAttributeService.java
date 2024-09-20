package com.powernobug.mall.product.service;

import com.powernobug.mall.product.dto.SaleAttributeInfoDTO;

import java.util.List;

public interface SalesAttributeService {

    /*
          查询所有的销售属性
     */
    List<SaleAttributeInfoDTO> getSaleAttrInfoList();
}
