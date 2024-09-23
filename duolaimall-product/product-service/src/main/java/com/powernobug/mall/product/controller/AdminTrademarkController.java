package com.powernobug.mall.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.product.dto.TrademarkDTO;
import com.powernobug.mall.product.dto.TrademarkPageDTO;
import com.powernobug.mall.product.model.Trademark;
import com.powernobug.mall.product.query.TrademarkParam;
import com.powernobug.mall.product.service.TrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.product.controller
 * @author: HuangWeiLong
 * @date: 2024/9/21 20:10
 */
@RestController
public class AdminTrademarkController {
    // http://localhost/admin/product/baseTrademark/1/10
// 查看品牌列表
    @Autowired
    TrademarkService trademarkService;
    @GetMapping("/admin/product/baseTrademark/{pageNo}/{pageSize}")
    public Result<TrademarkPageDTO> getTradeMarkDTOList(@PathVariable Long pageNo, @PathVariable Long pageSize) {
        Page<Trademark> pageParam = new Page<>(pageNo,pageSize);
        TrademarkPageDTO trademarkPageDTO = trademarkService.getPage(pageParam);
        return Result.ok(trademarkPageDTO);
    }


    // 保存品牌
//http://localhost/admin/product/baseTrademark/save
    @PostMapping("/admin/product/baseTrademark/save")
    public Result save(@RequestBody TrademarkParam trademarkParam){
        trademarkService.save(trademarkParam);
        return Result.ok();
    }

    // http://localhost/admin/product/baseTrademark/remove/10
// 删除品牌
    @DeleteMapping("/admin/product/baseTrademark/remove/{tradeMarkId}")
    public Result deleteById(@PathVariable Long tradeMarkId){
        trademarkService.removeById(tradeMarkId);
        return Result.ok();
    }


    // http://localhost/admin/product/baseTrademark/get/17
// 查询品牌
    @GetMapping("/admin/product/baseTrademark/get/{tradeMarkId}")
    public Result<TrademarkDTO> getTradeMarkDTO(@PathVariable Long tradeMarkId) {
        TrademarkDTO trademarkDTO = trademarkService.getTrademarkByTmId(tradeMarkId);
        return Result.ok(trademarkDTO);
    }

    // 修改品牌
// http://localhost/admin/product/baseTrademark/update
    @PutMapping("/admin/product/baseTrademark/update")
    public Result updateTradeMark(@RequestBody TrademarkParam trademarkParam){
        trademarkService.updateById(trademarkParam);
        return Result.ok();
    }
}
