package com.powernobug.mall.product.converter.dto;

import com.powernobug.mall.product.dto.*;
import com.powernobug.mall.product.model.CategoryHierarchy;
import com.powernobug.mall.product.model.FirstLevelCategory;
import com.powernobug.mall.product.model.SecondLevelCategory;
import com.powernobug.mall.product.model.ThirdLevelCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryConverter {

    FirstLevelCategoryDTO firstLevelCategoryPO2DTO(FirstLevelCategory firstLevelCategory);
    List<FirstLevelCategoryDTO> firstLevelCategoryPOs2DTOs(List<FirstLevelCategory> firstLevelCategories);

    SecondLevelCategoryDTO secondLevelCategoryPO2DTO(SecondLevelCategory secondLevelCategory);
    List<SecondLevelCategoryDTO> secondLevelCategoryPOs2DTOs(List<SecondLevelCategory> secondLevelCategories);

    ThirdLevelCategoryDTO thirdLevelCategoryPO2DTO(ThirdLevelCategory thirdLevelCategory);
    List<ThirdLevelCategoryDTO> thirdLevelCategoryPOs2DTOs(List<ThirdLevelCategory> thirdLevelCategories);
    @Mapping(source = "id",target = "categoryId")
    @Mapping(source = "name",target ="categoryName")
    ThirdLevelCategoryNodeDTO thirdLevelCategoryNodePO2DTO(ThirdLevelCategory thirdLevelCategory);
    List<ThirdLevelCategoryNodeDTO> thirdLevelCategoryNodePOs2DTOs(List<ThirdLevelCategory> thirdLevelCategories);

    @Mapping(source = "id",target = "categoryId")
    @Mapping(source = "name",target ="categoryName")
    SecondLevelCategoryNodeDTO secondLevelCategoryNodePO2DTO(SecondLevelCategory secondLevelCategory);
    List<SecondLevelCategoryNodeDTO> secondLevelCategoryNodePOs2DTOs(List<SecondLevelCategory> secondLevelCategories);

    @Mapping(source = "id",target = "categoryId")
    @Mapping(source = "name",target ="categoryName")
    FirstLevelCategoryNodeDTO firstLevelCategoryNodePO2DTO(FirstLevelCategory firstLevelCategory);
    List<FirstLevelCategoryNodeDTO> firstLevelCategoryNodePOs2DTOs(List<FirstLevelCategory> firstLevelCategories);




    CategoryHierarchyDTO categoryViewPO2DTO(CategoryHierarchy categoryHierarchy);

}
