package com.cskaoyan.mall.product.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class FirstLevelCategoryDTO {

   //"一级目录的id"
    private Long id;

    //"一级目录名称"
    private String name;
}
