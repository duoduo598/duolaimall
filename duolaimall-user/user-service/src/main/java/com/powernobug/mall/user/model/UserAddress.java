package com.powernobug.mall.user.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.powernobug.mall.common.model.BaseEntity;
import lombok.Data;

@Data
@TableName("user_address")
public class UserAddress extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField("user_address")
    private String userAddress;

    @TableField("user_id")
    private Long userId;

    @TableField("consignee")
    private String consignee;

    @TableField("phone_num")
    private String phoneNum;

    @TableField("is_default")
    private String isDefault;

}

