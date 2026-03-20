package com.ecommerce.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecommerce.dto.filter.CategoryFilter;
import com.ecommerce.dto.request.CreateCategoryRequest;
import com.ecommerce.dto.request.UpdateCategoryRequest;
import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.enums.CategoryStatus;
import com.ecommerce.mapper.CategoryMapper;
import com.ecommerce.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return CategoryMapper.toResponse(category);
    }
    
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Category name is required");
        }

        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category already exists");
        }

        Category category = new Category();
        category.setName(request.getName().trim());

        if (request.getParentId() != null) {

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));

            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);

        return CategoryMapper.toResponse(saved);
    }
    
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            category.setName(request.getName().trim());
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new RuntimeException("Category cannot be parent of itself");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));

            // Check circular reference
            Category current = parent;
            while (current != null) {
                if (current.getCategoryId().equals(id)) {
                    throw new RuntimeException("Cannot set a child as parent (circular reference)");
                }
                current = current.getParent();
            }
            category.setParent(parent);
        }

        categoryRepository.save(category);
        return CategoryMapper.toResponse(category);
    }
    
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        category.setStatus(CategoryStatus.INACTIVE);
        categoryRepository.save(category);
    }
    
    public Page<CategoryResponse> getCategories(Pageable pageable, String keyword) {
        if (keyword == null) {
            keyword = "";
        }
        return categoryRepository.findByNameContainingIgnoreCase(keyword, pageable)
                .map(CategoryMapper::toResponse);
    }
    
    public Page<CategoryResponse> getCategories(Pageable pageable, CategoryFilter filter) {
        return categoryRepository
                .findAllWithFilter(
                    filter.getKeyword(),
                    filter.getStatus(),
                    filter.getParentId() != null ? filter.getParentId() : -1L,
                    pageable
                )
                .map(CategoryMapper::toResponse);
    }


    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIsNullAndStatus(CategoryStatus.ACTIVE)
                .stream()
                .map(CategoryMapper::toResponse)
                .toList();
    }

}