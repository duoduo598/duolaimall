package com.powernobug.mall.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernobug.mall.product.converter.dto.TrademarkConverter;
import com.powernobug.mall.product.converter.dto.TrademarkPageConverter;
import com.powernobug.mall.product.dto.TrademarkDTO;
import com.powernobug.mall.product.dto.TrademarkPageDTO;
import com.powernobug.mall.product.mapper.TrademarkMapper;
import com.powernobug.mall.product.model.Trademark;
import com.powernobug.mall.product.query.TrademarkParam;
import com.powernobug.mall.product.service.TrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.service.impl
 * @author: HuangWeiLong
 * @date: 2024/9/21 20:14
 */
@Service
public class TrademarkServiceImpl implements TrademarkService {
    @Autowired
    TrademarkMapper trademarkMapper;
    @Autowired
    TrademarkPageConverter trademarkPageConverter;
    @Autowired
    TrademarkConverter trademarkConverter;
    @Override
    public TrademarkDTO getTrademarkByTmId(Long tmId) {
        Trademark trademark = trademarkMapper.selectById(tmId);
        return trademarkConverter.trademarkPO2DTO(trademark);
    }

    @Override
    public TrademarkPageDTO getPage(Page<Trademark> pageParam) {
        Page<Trademark> trademarkPage = trademarkMapper.selectPage(pageParam, null);
        return trademarkPageConverter.tradeMarkPagePO2PageDTO(trademarkPage);
    }

    @Override
    public void save(TrademarkParam trademarkParam) {
        Trademark trademark = trademarkConverter.trademarkParam2Trademark(trademarkParam);
        trademarkMapper.insert(trademark);
    }

    @Override
    public void updateById(TrademarkParam trademarkParam) {
        Trademark trademark = trademarkConverter.trademarkParam2Trademark(trademarkParam);
        trademarkMapper.updateById(trademark);
    }

    @Override
    public void removeById(Long id) {
        trademarkMapper.deleteById(id);
    }
}
