package com.cskaoyan.mall.product.service;

import com.cskaoyan.mall.product.dto.ProductTestDTO;

import javax.servlet.http.HttpServletRequest;

public interface TestProductService {

    // 方法返回值
    ProductTestDTO getProduct(Long id);

    void incrWithLock();
}
