package com.ecommerce.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.filter.CategoryFilter;
import com.ecommerce.dto.request.CreateCategoryRequest;
import com.ecommerce.dto.request.UpdateCategoryRequest;
import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.enums.CategoryStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.CategoryMapper;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.service.CategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return CategoryMapper.toResponse(category);
    }
    
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Category name is required");
        }

        if (categoryRepository.existsByCategoryName(request.getName().trim())) {
            throw new BadRequestException("Category already exists");
        }

        Category category = new Category();
        category.setCategoryName(request.getName().trim());
        category.setStatus(CategoryStatus.ACTIVE);

        if (request.getParentId() != null) {

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));

            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);

        return CategoryMapper.toResponse(saved);
    }
    
    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->new ResourceNotFoundException("Category not found"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            category.setCategoryName(request.getName().trim());
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("Category cannot be parent of itself");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));

            // Check circular reference
            Category current = parent;
            while (current != null) {
                if (current.getId().equals(id)) {
                    throw new BadRequestException("Cannot set a child as parent (circular reference)");
                }
                current = current.getParent();
            }
            category.setParent(parent);
        }

        categoryRepository.save(category);
        return CategoryMapper.toResponse(category);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        category.setStatus(CategoryStatus.INACTIVE);
        categoryRepository.save(category);
    }
    
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getCategories(Pageable pageable, String keyword) {
        if (keyword == null) {
            keyword = "";
        }
        return categoryRepository.findByCategoryNameContainingIgnoreCase(keyword, pageable)
                .map(CategoryMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getCategories(Pageable pageable, CategoryFilter filter) {
        return categoryRepository
                .findAllWithFilter(
                    filter.getKeyword(),
                    filter.getStatus(),
                    pageable
                )
                .map(CategoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIsNullAndStatus(CategoryStatus.ACTIVE)
                .stream()
                .map(CategoryMapper::toResponse)
                .toList();
    }
 
}