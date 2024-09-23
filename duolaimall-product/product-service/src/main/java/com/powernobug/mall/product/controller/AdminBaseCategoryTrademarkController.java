package com.powernobug.mall.product.controller;

import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.product.dto.TrademarkDTO;
import com.powernobug.mall.product.query.CategoryTrademarkParam;
import com.powernobug.mall.product.service.BaseCategoryTrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.controller
 * @author: HuangWeiLong
 * @date: 2024/9/21 21:12
 */
@RestController
public class AdminBaseCategoryTrademarkController {
    @Autowired
    BaseCategoryTrademarkService baseCategoryTrademarkService;

    //添加商品类目品牌
    @PostMapping("admin/product/baseCategoryTrademark/save")
    public Result save(@RequestBody CategoryTrademarkParam categoryTrademarkVo){
        baseCategoryTrademarkService.save(categoryTrademarkVo);
        return Result.ok();
    }

    //查询商品类目品牌
    @GetMapping("admin/product/baseCategoryTrademark/findTrademarkList/{thirdLevelCategoryId}")
    public Result findTrademarkList(@PathVariable Long thirdLevelCategoryId){
        List<TrademarkDTO> trademarkList = baseCategoryTrademarkService.findTrademarkList(thirdLevelCategoryId);
        if(trademarkList==null){
            return Result.ok(null);
        }
        return Result.ok(trademarkList);
    }

    //查询未关联商品类目的品牌
    @GetMapping("admin/product/baseCategoryTrademark/findCurrentTrademarkList/{thirdLevelCategoryId}")
    public Result findCurrentTrademarkList(@PathVariable Long thirdLevelCategoryId){
        List<TrademarkDTO> unLinkedTrademarkList = baseCategoryTrademarkService.findUnLinkedTrademarkList(thirdLevelCategoryId);
        return Result.ok(unLinkedTrademarkList);
    }

    //删除目录品牌关联
    @DeleteMapping("admin/product/baseCategoryTrademark/remove/{thirdLevelCategoryId}/{trademarkId}")
    public Result remove(@PathVariable Long thirdLevelCategoryId, @PathVariable Long trademarkId){
        baseCategoryTrademarkService.remove(thirdLevelCategoryId,trademarkId);
        return Result.ok();
    }


}
