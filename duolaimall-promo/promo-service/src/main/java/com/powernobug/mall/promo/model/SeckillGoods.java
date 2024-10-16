package com.powernobug.mall.promo.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.powernobug.mall.common.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("seckill_goods")
public class SeckillGoods extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@TableField("spu_id")
	private Long spuId;

	@TableField("sku_id")
	private Long skuId;

	@TableField("sku_name")
	private String skuName;

	@TableField("sku_default_img")
	private String skuDefaultImg;

	@TableField("price")
	private BigDecimal price;

	// 秒杀价格
	@TableField("cost_price")
	private BigDecimal costPrice;

	// 添加日期
	@TableField("create_time")
	private Date createTime;

	// 审核日期
	@TableField("check_time")
	private Date checkTime;

	// 审核状态
	@TableField("status")
	private String status;

	// 开始时间
	@TableField("start_time")
	private Date startTime;

	// 结束时间
	@TableField("end_time")
	private Date endTime;

	// 秒杀商品数
	@TableField("num")
	private Integer num;

	// 剩余库存数
	@TableField("stock_count")
	private Integer stockCount;

	@TableField("sku_desc")
	private String skuDesc;

}

