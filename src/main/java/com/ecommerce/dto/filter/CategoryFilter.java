package com.ecommerce.dto.filter;

import com.ecommerce.enums.CategoryStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryFilter {

    private String keyword;           
    private CategoryStatus status;   

}