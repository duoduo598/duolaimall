package com.powernobug.mall.product.controller;

import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.product.dto.PlatformAttributeInfoDTO;
import com.powernobug.mall.product.service.PlatformAttributeService;
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
 * @date: 2024/9/20 20:39
 */
@RestController
public class AdminPlatformController {
    @Autowired
    PlatformAttributeService platformAttributeService;
    // 根据分类Id查询平台属性以及平台属性值
// http://localhost/admin/product/attrInfoList/3/20/149
    @GetMapping("/admin/product/attrInfoList/{firstLevelCategoryId}/{secondLevelCategoryId}/{thirdLevelCategoryId}")
    public Result getAttrInfoList(@PathVariable Long firstLevelCategoryId,
                                  @PathVariable Long secondLevelCategoryId,
                                  @PathVariable Long thirdLevelCategoryId){
        List<PlatformAttributeInfoDTO> platformAttrInfoList = platformAttributeService.getPlatformAttrInfoList(firstLevelCategoryId, secondLevelCategoryId, thirdLevelCategoryId);
        return Result.ok(platformAttrInfoList);
    }


}
