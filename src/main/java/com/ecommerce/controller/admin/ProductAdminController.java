package com.ecommerce.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.dto.filter.ProductFilter;
import com.ecommerce.dto.request.CreateProductRequest;
import com.ecommerce.dto.request.UpdateProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.service.ProductService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class ProductAdminController {

    private final ProductService productService;
    
    public ProductAdminController(ProductService productService) {
        this.productService = productService;
    }
  
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
    	ProductResponse response = productService.createProduct(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request ) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/images")
    public ResponseEntity<Void> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") List<MultipartFile> files
    ) {
    	productService.uploadImages(id, files);
    	return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    	 productService.hideProduct(id);
    	 return ResponseEntity.ok().build();
    }
}