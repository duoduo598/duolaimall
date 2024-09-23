package com.powernobug.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernobug.mall.product.converter.dto.TrademarkConverter;
import com.powernobug.mall.product.dto.TrademarkDTO;
import com.powernobug.mall.product.mapper.CategoryTrademarkMapper;
import com.powernobug.mall.product.mapper.TrademarkMapper;
import com.powernobug.mall.product.model.CategoryTrademark;
import com.powernobug.mall.product.model.Trademark;
import com.powernobug.mall.product.query.CategoryTrademarkParam;
import com.powernobug.mall.product.service.BaseCategoryTrademarkService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.service.impl
 * @author: HuangWeiLong
 * @date: 2024/9/21 21:19
 */
@Service
public class BaseCategoryTrademarkServiceImpl implements BaseCategoryTrademarkService {
    @Autowired
    CategoryTrademarkMapper categoryTrademarkMapper;
    @Autowired
    TrademarkMapper trademarkMapper;
    @Autowired
    TrademarkConverter trademarkConverter;
    @Override
    public void save(CategoryTrademarkParam categoryTrademarkParam) {
        List<Long> trademarkIdList = categoryTrademarkParam.getTrademarkIdList();
        Long category3Id = categoryTrademarkParam.getCategory3Id();
        trademarkIdList.forEach(aLong -> {
            CategoryTrademark categoryTrademark = new CategoryTrademark();
            categoryTrademark.setThirdLevelCategoryId(category3Id);
            categoryTrademark.setTrademarkId(aLong);
            categoryTrademarkMapper.insert(categoryTrademark);
        });
    }

    @Override
    public List<TrademarkDTO> findTrademarkList(Long category3Id) {
        //先根据三级类目id，在类目品牌关联表category_trademark中，找到属于该类目的所有品牌的id即trademark_id集合
        List<Long> trademarkIds = getRelatedTrademarkIds(category3Id);
        //然后，在根据trademark_id集合，在trademark表中查询出每个品牌的详细信息
        LambdaQueryWrapper<Trademark> wrapper = new LambdaQueryWrapper<>();
        if(!trademarkIds.isEmpty()){
        wrapper.in(Trademark::getId,trademarkIds);
        List<Trademark> trademarks = trademarkMapper.selectList(wrapper);
        return trademarkConverter.trademarkPOs2DTOs(trademarks);
        }
       return null;
    }

    @NotNull
    private List<Long> getRelatedTrademarkIds(Long category3Id) {
        LambdaQueryWrapper<CategoryTrademark> categoryTrademarkLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryTrademarkLambdaQueryWrapper.eq(CategoryTrademark::getThirdLevelCategoryId, category3Id);
        List<CategoryTrademark> trademarkList = categoryTrademarkMapper.selectList(categoryTrademarkLambdaQueryWrapper);
        List<Long> trademarkIds = trademarkList.stream().map(CategoryTrademark::getTrademarkId).toList();
        return trademarkIds;
    }

    @Override
    public List<TrademarkDTO> findUnLinkedTrademarkList(Long thirdLevelCategoryId) {
        //先根据三级类目id，在类目品牌关联表category_trademark中，找到属于该类目的所有品牌的id即trademark_id集合
        List<Long> trademarkIds = getRelatedTrademarkIds(thirdLevelCategoryId);
        //然后，在根据trademark_id集合，在trademark表中查询出每个品牌的详细信息
        LambdaQueryWrapper<Trademark> wrapper = new LambdaQueryWrapper<>();
        if(!trademarkIds.isEmpty()){
            wrapper.notIn(Trademark::getId,trademarkIds);
            List<Trademark> trademarks = trademarkMapper.selectList(wrapper);
            return trademarkConverter.trademarkPOs2DTOs(trademarks);
        }
        List<Trademark> trademarks = trademarkMapper.selectList(null);
        return trademarkConverter.trademarkPOs2DTOs(trademarks);
    }

    @Override
    public void remove(Long thirdLevelCategoryId, Long trademarkId) {
        LambdaQueryWrapper<CategoryTrademark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryTrademark::getThirdLevelCategoryId,thirdLevelCategoryId);
        wrapper.eq(CategoryTrademark::getTrademarkId,trademarkId);
        categoryTrademarkMapper.delete(wrapper);
    }
}
