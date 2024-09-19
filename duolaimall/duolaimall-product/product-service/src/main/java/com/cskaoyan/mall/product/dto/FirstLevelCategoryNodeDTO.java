package com.cskaoyan.mall.product.dto;

import lombok.Data;

import java.util.List;

@Data
public class FirstLevelCategoryNodeDTO {

    //"一级目录的id"
    Long categoryId;

   // "一级目录的名称"
    String categoryName;

    // "一级目录所包含的二级目录"
    List<SecondLevelCategoryNodeDTO> categoryChild;

}
