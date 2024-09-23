package com.powernobug.mall.product.service;

import com.powernobug.mall.product.dto.TrademarkDTO;
import com.powernobug.mall.product.query.CategoryTrademarkParam;

import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.service
 * @author: HuangWeiLong
 * @date: 2024/9/21 21:16
 */
public interface BaseCategoryTrademarkService {
    /**
     * 保存分类与品牌关联
     */
    void save(CategoryTrademarkParam categoryTrademarkParam);
    /**
     * 根据三级分类获取品牌
     */
    List<TrademarkDTO> findTrademarkList(Long category3Id);
    /**
     * 获取当前未被三级分类关联的所有品牌
     */
    List<TrademarkDTO> findUnLinkedTrademarkList(Long thirdLevelCategoryId);
    /**
     * 删除关联
     */
    void remove(Long thirdLevelCategoryId, Long trademarkId);
}
