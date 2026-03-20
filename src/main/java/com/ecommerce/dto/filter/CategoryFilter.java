package com.ecommerce.dto.filter;

import com.ecommerce.enums.CategoryStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryFilter {

    private String keyword;           
    private CategoryStatus status;   
    private Long parentId;

}