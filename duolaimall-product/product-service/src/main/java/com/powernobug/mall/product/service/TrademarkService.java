package com.powernobug.mall.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernobug.mall.product.dto.TrademarkDTO;
import com.powernobug.mall.product.dto.TrademarkPageDTO;
import com.powernobug.mall.product.model.Trademark;
import com.powernobug.mall.product.query.TrademarkParam;

public interface TrademarkService {
    /*
        根据品牌id，查询品牌
     */
    TrademarkDTO getTrademarkByTmId(Long tmId);
    /**
     * 根据分页参数，分页查询品牌列表
     */
    TrademarkPageDTO getPage(Page<Trademark> pageParam);
    /*
         保存品牌
     */
    void save(TrademarkParam trademarkParam);
    /*
          更新品牌
     */
    void updateById(TrademarkParam trademarkParam);
    /*
        删除品牌
     */
    void removeById(Long id);
}
