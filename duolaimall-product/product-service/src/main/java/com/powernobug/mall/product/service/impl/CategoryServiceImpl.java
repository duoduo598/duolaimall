package com.powernobug.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernobug.mall.product.converter.dto.CategoryConverter;
import com.powernobug.mall.product.dto.*;
import com.powernobug.mall.product.mapper.FirstLevelCategoryMapper;
import com.powernobug.mall.product.mapper.SecondLevelCategoryMapper;
import com.powernobug.mall.product.mapper.ThirdLevelCategoryMapper;
import com.powernobug.mall.product.model.CategoryHierarchy;
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
        CategoryHierarchy categoryHierarchy = new CategoryHierarchy();
        //三级分类
        LambdaQueryWrapper<ThirdLevelCategory> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(ThirdLevelCategory::getId,thirdLevelCategoryId);
        ThirdLevelCategory thirdLevelCategory = thirdLevelCategoryMapper.selectOne(wrapper1);
        if(thirdLevelCategory!=null){
            String thirdLevelCategoryName = thirdLevelCategory.getName();
            categoryHierarchy.setThirdLevelCategoryId(thirdLevelCategoryId);
            categoryHierarchy.setThirdLevelCategoryName(thirdLevelCategoryName);
            Long secondLevelCategoryId = thirdLevelCategory.getSecondLevelCategoryId();
            //二级分类
            LambdaQueryWrapper<SecondLevelCategory> wrapper2 = new LambdaQueryWrapper<>();
            wrapper2.eq(SecondLevelCategory::getId,secondLevelCategoryId);
            SecondLevelCategory secondLevelCategory = secondLevelCategoryMapper.selectOne(wrapper2);
            String secondLevelCategoryName = secondLevelCategory.getName();
            categoryHierarchy.setSecondLevelCategoryId(secondLevelCategoryId);
            categoryHierarchy.setSecondLevelCategoryName(secondLevelCategoryName);
            Long firstLevelCategoryId = secondLevelCategory.getFirstLevelCategoryId();
            //一级分类
            LambdaQueryWrapper<FirstLevelCategory> wrapper3 = new LambdaQueryWrapper<>();
            wrapper3.eq(FirstLevelCategory::getId,firstLevelCategoryId);
            FirstLevelCategory firstLevelCategory = firstLevelCategoryMapper.selectOne(wrapper3);
            String firstLevelCategoryName = firstLevelCategory.getName();
            categoryHierarchy.setFirstLevelCategoryId(firstLevelCategoryId);
            categoryHierarchy.setFirstLevelCategoryName(firstLevelCategoryName);
        }
        return categoryConverter.categoryViewPO2DTO(categoryHierarchy);
    }

    @Override
    public List<FirstLevelCategoryNodeDTO> getCategoryTreeList() {
        return null;
    }
}
