package com.powernobug.mall.search.controller;

import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.search.dto.SearchResponseDTO;
import com.powernobug.mall.search.param.SearchParam;
import com.powernobug.mall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.search.controller
 * @author: HuangWeiLong
 * @date: 2024/10/6 16:28
 */
@RestController
public class SearchApiController {
    @Autowired
    SearchService searchService;
    @GetMapping("/api/list/inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable("skuId") Long skuId){
        searchService.upperGoods(skuId);
        return Result.ok();
    }

    @GetMapping("/api/list/inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable("skuId") Long skuId){
        searchService.lowerGoods(skuId);
        return Result.ok();
    }

    /**
     * 更新商品incrHotScore
     * @param skuId
     * @return
     */
    @GetMapping("/api/list/inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable("skuId") Long skuId){
        searchService.incrHotScore(skuId);
        return Result.ok();
    }
    @GetMapping("/list")
    public Result list(SearchParam searchParam) throws IOException {
        SearchResponseDTO search = searchService.search(searchParam);
        return Result.ok(search);
    }
}
