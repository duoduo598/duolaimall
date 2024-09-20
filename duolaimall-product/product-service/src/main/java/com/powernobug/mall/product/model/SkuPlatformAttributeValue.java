package com.powernobug.mall.product.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.powernobug.mall.common.model.BaseEntity;
import lombok.Data;

/**
 * <p>
 * SkuPlatformAttributeValue
 * </p>
 *
 */
@Data
//"Sku平台属性值"
@TableName("sku_platform_attr_value")
public class SkuPlatformAttributeValue extends BaseEntity {

	private static final long serialVersionUID = 1L;

	//"属性id"
	@TableField("attr_id")
	private Long attrId;

	//"属性值id"
	@TableField("value_id")
	private Long valueId;

	//"skuid"
	@TableField("sku_id")
	private Long skuId;

}

