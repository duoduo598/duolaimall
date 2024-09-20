package com.powernobug.mall.product.converter.dto;

import com.powernobug.mall.product.dto.CategoryHierarchyDTO;
import com.powernobug.mall.product.dto.FirstLevelCategoryDTO;
import com.powernobug.mall.product.dto.SecondLevelCategoryDTO;
import com.powernobug.mall.product.dto.ThirdLevelCategoryDTO;
import com.powernobug.mall.product.model.CategoryHierarchy;
import com.powernobug.mall.product.model.FirstLevelCategory;
import com.powernobug.mall.product.model.SecondLevelCategory;
import com.powernobug.mall.product.model.ThirdLevelCategory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryConverter {

    FirstLevelCategoryDTO firstLevelCategoryPO2DTO(FirstLevelCategory firstLevelCategory);
    List<FirstLevelCategoryDTO> firstLevelCategoryPOs2DTOs(List<FirstLevelCategory> firstLevelCategories);

    SecondLevelCategoryDTO secondLevelCategoryPO2DTO(SecondLevelCategory secondLevelCategory);
    List<SecondLevelCategoryDTO> secondLevelCategoryPOs2DTOs(List<SecondLevelCategory> secondLevelCategories);

    ThirdLevelCategoryDTO thirdLevelCategoryPO2DTO(ThirdLevelCategory thirdLevelCategory);
    List<ThirdLevelCategoryDTO> thirdLevelCategoryPOs2DTOs(List<ThirdLevelCategory> thirdLevelCategories);

    CategoryHierarchyDTO categoryViewPO2DTO(CategoryHierarchy categoryHierarchy);

}
