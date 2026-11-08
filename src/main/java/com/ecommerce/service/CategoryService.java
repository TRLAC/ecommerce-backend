package com.ecommerce.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.dto.filter.CategoryFilter;
import com.ecommerce.dto.request.CreateCategoryRequest;
import com.ecommerce.dto.request.UpdateCategoryRequest;
import com.ecommerce.dto.response.CategoryResponse;

public interface CategoryService {
    CategoryResponse getCategoryById(Long id);
    CategoryResponse createCategory(CreateCategoryRequest request);
    CategoryResponse updateCategory(Long id, UpdateCategoryRequest request);
    void deleteCategory(Long id);
    Page<CategoryResponse> getCategories(Pageable pageable, String keyword);
    Page<CategoryResponse> getCategories(Pageable pageable, CategoryFilter filter);
    List<CategoryResponse> getRootCategories();
}