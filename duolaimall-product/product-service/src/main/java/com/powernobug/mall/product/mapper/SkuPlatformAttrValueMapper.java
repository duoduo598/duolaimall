package com.powernobug.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powernobug.mall.product.model.PlatformAttributeInfo;
import com.powernobug.mall.product.model.SkuPlatformAttributeValue;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SkuPlatformAttrValueMapper extends BaseMapper<SkuPlatformAttributeValue> {
    List<PlatformAttributeInfo> selectPlatformAttrInfoBySku(Long skuId);
}
