package com.ecommerce.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.dto.filter.ProductFilter;
import com.ecommerce.dto.request.CreateProductRequest;
import com.ecommerce.dto.request.UpdateProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.entity.Product;

public interface ProductService {
    Product findById(Long id);
    void validateStock(Long productId, int qty);
    void deductStock(Long productId, int qty);
    void restoreStock(Long productId, int qty);
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse updateProduct(Long id, UpdateProductRequest request);
    void uploadImages(Long productId, List<MultipartFile> files);
    void hideProduct(Long id);
    Page<ProductResponse> getProducts(Pageable pageable, ProductFilter filter);
    ProductResponse getProductById(Long id);
}