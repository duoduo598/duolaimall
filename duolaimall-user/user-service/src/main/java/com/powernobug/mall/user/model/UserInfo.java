package com.powernobug.mall.user.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.powernobug.mall.common.model.BaseEntity;
import lombok.Data;

@Data
@TableName("user_info")
public class UserInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField("login_name")
    private String loginName;

    @TableField("nick_name")
    private String nickName;

    @TableField("passwd")
    private String passwd;

    @TableField("name")
    private String name;

    @TableField("phone_num")
    private String phoneNum;

    @TableField("email")
    private String email;

    @TableField("head_img")
    private String headImg;

    @TableField("user_level")
    private String userLevel;

}
