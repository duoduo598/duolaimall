package com.powernobug.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernobug.mall.product.cache.RedisCache;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @RedisCache(prefix = "product:detail:category:")
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
        //判空，查出List<ThirdLevelCategory>
        LambdaQueryWrapper<ThirdLevelCategory> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.isNotNull(ThirdLevelCategory::getSecondLevelCategoryId);
        List<ThirdLevelCategory> thirdLevelCategories = thirdLevelCategoryMapper.selectList(wrapper1);
        //封装所有的三级类目
        Map<Long, List<ThirdLevelCategory>> collect1 = thirdLevelCategories.stream().collect(Collectors.groupingBy(ThirdLevelCategory::getSecondLevelCategoryId));
        //新建一个map来接收，转换完的ThirdLevelCategoryNodeDTO
        Map<Long, List<ThirdLevelCategoryNodeDTO>> newcollect1=new HashMap<>();
        collect1.forEach((key, value) -> newcollect1.put(key, categoryConverter.thirdLevelCategoryNodePOs2DTOs(value)));



        //判空，查未封装categoryChild的List<SecondLevelCategory>
        LambdaQueryWrapper<SecondLevelCategory> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.isNotNull(SecondLevelCategory::getFirstLevelCategoryId);
        List<SecondLevelCategory> secondLevelCategories = secondLevelCategoryMapper.selectList(wrapper2);
        //封装所有的二级类目
        Map<Long, List<SecondLevelCategory>> collect2 = secondLevelCategories.stream().collect(Collectors.groupingBy(SecondLevelCategory::getFirstLevelCategoryId));
        //新建一个map来接收，转换完的SecondLevelCategoryNodeDTO
        Map<Long, List<SecondLevelCategoryNodeDTO>> newcollect2=new HashMap<>();
        collect2.forEach((key, value) -> newcollect2.put(key, categoryConverter.secondLevelCategoryNodePOs2DTOs(value)));
        //为newcollect2中每个list中SecondLevelCategoryNodeDTO对象赋值
        newcollect2.values().forEach(secondLevelCategoryNodeDTOS -> secondLevelCategoryNodeDTOS.forEach(secondLevelCategoryNodeDTO -> {
            Long categoryId = secondLevelCategoryNodeDTO.getCategoryId();
            secondLevelCategoryNodeDTO.setCategoryChild(newcollect1.get(categoryId));
        }));



        //判空，查未封装categoryChild的List<FirstLevelCategory>
        LambdaQueryWrapper<FirstLevelCategory> wrapper3 = new LambdaQueryWrapper<>();
        wrapper3.isNotNull(FirstLevelCategory::getId);
        List<FirstLevelCategory> firstLevelCategories = firstLevelCategoryMapper.selectList(wrapper3);
        //转换为DTO对象list
        List<FirstLevelCategoryNodeDTO> firstLevelCategoryNodeDTOS = categoryConverter.firstLevelCategoryNodePOs2DTOs(firstLevelCategories);
        //遍历封装
        firstLevelCategoryNodeDTOS.forEach(firstLevelCategoryNodeDTO -> {
            Long categoryId = firstLevelCategoryNodeDTO.getCategoryId();
            firstLevelCategoryNodeDTO.setCategoryChild(newcollect2.get(categoryId));
        });


        return firstLevelCategoryNodeDTOS;
    }
}
