package com.powernobug.mall.product.service;

import com.powernobug.mall.product.dto.ProductTestDTO;

public interface TestProductService {

    // 方法返回值
    ProductTestDTO getProduct(Long id);

    void incrWithLock();
}
