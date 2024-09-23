package com.powernobug.mall.product.controller;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.product.converter.dto.PlatformAttributeInfoConverter;
import com.powernobug.mall.product.converter.param.PlatformAttributeInfoParamConverter;
import com.powernobug.mall.product.dto.PlatformAttributeInfoDTO;
import com.powernobug.mall.product.dto.PlatformAttributeValueDTO;
import com.powernobug.mall.product.mapper.PlatformAttrInfoMapper;
import com.powernobug.mall.product.mapper.PlatformAttrValueMapper;
import com.powernobug.mall.product.model.PlatformAttributeInfo;
import com.powernobug.mall.product.model.PlatformAttributeValue;
import com.powernobug.mall.product.query.PlatformAttributeParam;
import com.powernobug.mall.product.service.PlatformAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    PlatformAttributeInfoConverter platformAttributeInfoConverter;
    // 根据分类Id查询平台属性以及平台属性值
// http://localhost/admin/product/attrInfoList/3/20/149
    @GetMapping("/admin/product/attrInfoList/{firstLevelCategoryId}/{secondLevelCategoryId}/{thirdLevelCategoryId}")
    public Result getAttrInfoList(@PathVariable Long firstLevelCategoryId,
                                  @PathVariable Long secondLevelCategoryId,
                                  @PathVariable Long thirdLevelCategoryId){
        List<PlatformAttributeInfoDTO> platformAttrInfoList = platformAttributeService.getPlatformAttrInfoList(firstLevelCategoryId, secondLevelCategoryId, thirdLevelCategoryId);
        return Result.ok(platformAttrInfoList);
    }

    /**
     * 2. 平台属性的保存
     */
    // 保存平台属性
    //  http://localhost/admin/product/saveAttrInfo
    @PostMapping("/admin/product/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody PlatformAttributeParam platformAttributeParam) {
        platformAttributeService.savePlatformAttrInfo(platformAttributeParam);
        return Result.ok();
    }

    // http://localhost/admin/product/getAttrValueList/106
// 平台属性值回显
    @GetMapping("/admin/product/getAttrValueList/{attrId}")
    public Result<List<PlatformAttributeValueDTO>> getAttrInfoDTO(@PathVariable Long attrId) {
        List<PlatformAttributeValueDTO> attributeValueDTOS = platformAttributeService.getPlatformAttrInfo(attrId);
        return  Result.ok(attributeValueDTOS);
    }

}
