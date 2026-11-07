package com.ecommerce.mapper;

import org.springframework.stereotype.Component;

import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.entity.Category;

public class CategoryMapper {

    public static CategoryResponse toResponse(Category category) {
        CategoryResponse resp = new CategoryResponse();
        resp.setCategoryId(category.getId());
        resp.setCategoryName(category.getCategoryName());
        resp.setParentId(category.getParent() != null ? category.getParent().getId() : null);
        resp.setStatus(category.getStatus().name());
        resp.setCreatedAt(category.getCreatedAt());
        resp.setUpdatedAt(category.getUpdatedAt());
        return resp;
    }
}