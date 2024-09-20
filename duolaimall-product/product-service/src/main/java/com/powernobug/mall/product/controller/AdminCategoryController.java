package com.powernobug.mall.product.controller;

import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.product.dto.FirstLevelCategoryDTO;
import com.powernobug.mall.product.dto.SecondLevelCategoryDTO;
import com.powernobug.mall.product.dto.ThirdLevelCategoryDTO;
import com.powernobug.mall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.controller
 * @author: HuangWeiLong
 * @date: 2024/9/20 19:58
 */
@RestController
public class AdminCategoryController {
    @Autowired
    CategoryService categoryService;
    // 查询一级分类
    @GetMapping("admin/product/getCategory1")
    public Result<List<FirstLevelCategoryDTO>> getCategory1(){
        List<FirstLevelCategoryDTO> firstLevelCategory = categoryService.getFirstLevelCategory();
        return Result.ok(firstLevelCategory);
    }

    // 根据一级分类查询二级分类
    @GetMapping("/admin/product/getCategory2/{firstLevelCategoryId}")
    public Result<List<SecondLevelCategoryDTO>> getCategory2(@PathVariable Long firstLevelCategoryId){
        List<SecondLevelCategoryDTO> secondLevelCategory = categoryService.getSecondLevelCategory(firstLevelCategoryId);
        return Result.ok(secondLevelCategory);
    }

    // 根据二级分类，查询三级分类
    @GetMapping("/admin/product/getCategory3/{category2Id}")
    public Result<List<ThirdLevelCategoryDTO>> getCategory3(@PathVariable Long category2Id){
        List<ThirdLevelCategoryDTO> thirdLevelCategory = categoryService.getThirdLevelCategory(category2Id);
        return Result.ok(thirdLevelCategory);
    }
}
