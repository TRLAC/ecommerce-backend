package com.ecommerce.mapper;

import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.entity.Category;

public class CategoryMapper {

    public static CategoryResponse toResponse(Category category) {

        CategoryResponse res = new CategoryResponse();

        res.setCategoryId(category.getCategoryId());
        res.setName(category.getName());

        if (category.getParent() != null) {
            res.setParentId(category.getParent().getCategoryId());
        }

        return res;
    }
}
