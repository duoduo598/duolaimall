package com.powernobug.mall.product.controller;

import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.product.dto.FirstLevelCategoryNodeDTO;
import com.powernobug.mall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.controller
 * @author: HuangWeiLong
 * @date: 2024/9/25 21:21
 */
@RestController
public class IndexController {
    @Autowired
    CategoryService categoryService;
    /**
     * 获取全部分类信息
     * @return
     */
    @GetMapping("/index")
    public Result getBaseCategoryList(){
        List<FirstLevelCategoryNodeDTO> categoryTreeList = categoryService.getCategoryTreeList();
        return Result.ok(categoryTreeList);
    }
}
