package com.ecommerce.mapper;

import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.entity.Category;

public class CategoryMapper {

    public static CategoryResponse toResponse(Category category) {
    	 if (category == null) {
             return null;
         }

        CategoryResponse res = new CategoryResponse();

        res.setCategoryId(category.getCategoryId());
        res.setName(category.getName());
        

        if (category.getParent() != null) {
            res.setParentId(category.getParent().getCategoryId());
        }
        
        res.setCreatedAt(category.getCreatedAt());
        res.setUpdatedAt(category.getUpdatedAt());

        return res;
    }
}
