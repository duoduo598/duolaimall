package com.powernobug.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernobug.mall.product.converter.dto.CategoryConverter;
import com.powernobug.mall.product.dto.*;
import com.powernobug.mall.product.mapper.FirstLevelCategoryMapper;
import com.powernobug.mall.product.mapper.SecondLevelCategoryMapper;
import com.powernobug.mall.product.mapper.ThirdLevelCategoryMapper;
import com.powernobug.mall.product.model.FirstLevelCategory;
import com.powernobug.mall.product.model.SecondLevelCategory;
import com.powernobug.mall.product.model.ThirdLevelCategory;
import com.powernobug.mall.product.query.CategoryTrademarkParam;
import com.powernobug.mall.product.service.CategoryService;
import com.powernobug.mall.product.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.service.impl
 * @author: HuangWeiLong
 * @date: 2024/9/20 20:15
 */
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    FirstLevelCategoryMapper firstLevelCategoryMapper;
    @Autowired
    SecondLevelCategoryMapper secondLevelCategoryMapper;
    @Autowired
    ThirdLevelCategoryMapper thirdLevelCategoryMapper;
    @Autowired
    CategoryConverter categoryConverter;
    @Override
    public List<FirstLevelCategoryDTO> getFirstLevelCategory() {
        List<FirstLevelCategory> firstLevelCategories = firstLevelCategoryMapper.selectList(null);
        return categoryConverter.firstLevelCategoryPOs2DTOs(firstLevelCategories);
    }

    @Override
    public List<SecondLevelCategoryDTO> getSecondLevelCategory(Long firstLevelCategoryId) {
        LambdaQueryWrapper<SecondLevelCategory> secondWrapper = new LambdaQueryWrapper<>();
        secondWrapper.eq(SecondLevelCategory::getFirstLevelCategoryId,firstLevelCategoryId);
        List<SecondLevelCategory> secondLevelCategories = secondLevelCategoryMapper.selectList(secondWrapper);
        return categoryConverter.secondLevelCategoryPOs2DTOs(secondLevelCategories);
    }

    @Override
    public List<ThirdLevelCategoryDTO> getThirdLevelCategory(Long secondLevelCategoryId) {
        LambdaQueryWrapper<ThirdLevelCategory> thirdWrapper = new LambdaQueryWrapper<>();
        thirdWrapper.eq(ThirdLevelCategory::getSecondLevelCategoryId, secondLevelCategoryId);
        List<ThirdLevelCategory> thirdLevelCategories = thirdLevelCategoryMapper.selectList(thirdWrapper);
        return categoryConverter.thirdLevelCategoryPOs2DTOs(thirdLevelCategories);
    }

    @Override
    public List<TrademarkDTO> findTrademarkList(Long category3Id) {
        return null;
    }

    @Override
    public void save(CategoryTrademarkParam categoryTrademarkParam) {

    }

    @Override
    public List<TrademarkDTO> findUnLinkedTrademarkList(Long thirdLevelCategoryId) {
        return null;
    }

    @Override
    public void remove(Long thirdLevelCategoryId, Long trademarkId) {

    }

    @Override
    public CategoryHierarchyDTO getCategoryViewByCategoryId(Long thirdLevelCategoryId) {
        return null;
    }

    @Override
    public List<FirstLevelCategoryNodeDTO> getCategoryTreeList() {
        return null;
    }
}
