package com.powernobug.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powernobug.mall.product.model.SkuSaleAttributeValue;
import com.powernobug.mall.product.model.SkuSaleAttributeValuePermutation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttributeValue> {

    // 根据spuId 查询map 集合数据
    List<SkuSaleAttributeValuePermutation> selectSaleAttrValuesBySpu(Long spuId);

}
