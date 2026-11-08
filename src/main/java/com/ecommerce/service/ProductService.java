package com.ecommerce.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.dto.filter.ProductFilter;
import com.ecommerce.dto.request.CreateProductRequest;
import com.ecommerce.dto.request.UpdateProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.entity.Brand;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductImage;
import com.ecommerce.enums.ProductStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.repository.BrandRepository;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductImageRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductSpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
	private final ProductRepository productRepository;
	private final BrandRepository brandRepository;
	private final CategoryRepository categoryRepository;
	private final FileStorageService fileStorageService;
	private final ProductImageRepository productImageRepository;

	
	public Product findById(Long id) {
	    return productRepository.findById(id)
	            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
	}
	
	public void validateStock(Long productId, int qty) {

	     Product product = findById(productId);

	     if (product.getStockQuantity() < qty) {
	         throw new BadRequestException(
	                 "Insufficient stock: " + product.getName()
	         );
	     }
	 }
	
	@Transactional
	public void deductStock(Long productId, int qty) {
	    int updated = productRepository.deductStock(productId, qty);
	    if (updated == 0) {
	        // Không trừ được: hoặc sản phẩm không tồn tại (404), hoặc không đủ hàng (400)
	        Product product = findById(productId);
	        throw new BadRequestException("Insufficient stock for product: " + product.getName());
	    }
	}

	@Transactional
	public void restoreStock(Long productId, int qty) {
	    productRepository.restoreStock(productId, qty);
	}
	
	@Transactional
	public ProductResponse createProduct(CreateProductRequest request) {
		 
		 Brand brand = brandRepository.findById(request.getBrandId())
	                .orElseThrow(() -> new RuntimeException("Brand not found"));
		 
		 Category category = categoryRepository.findById(request.getCategoryId())
	                .orElseThrow(() -> new RuntimeException("Category not found"));
		 
		 Product product = new Product();
		 product.setName(request.getName());
	     product.setBrand(brand);
	     product.setCategory(category);
	     product.setPrice(request.getPrice());
	     product.setStockQuantity(request.getStockQuantity());
	     product.setRam(request.getRam());
	     product.setStorage(request.getStorage());
	     product.setBattery(request.getBattery());
		 product.setScreenSize(request.getScreenSize());
		 product.setStatus(ProductStatus.ACTIVE);
	     
	     Product saved = productRepository.save(product);
	     
	     return ProductMapper.toResponse(saved);
	}
	
	@Transactional
	public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
		
		Product product = findById(id);
		
		if (request.getBrandId() != null) {
	        Brand brand = brandRepository.findById(request.getBrandId())
	                .orElseThrow(() -> new RuntimeException("Brand not found"));
	        product.setBrand(brand);
	    }

	    if (request.getCategoryId() != null) {
	        Category category = categoryRepository.findById(request.getCategoryId())
	                .orElseThrow(() -> new RuntimeException("Category not found"));
	        product.setCategory(category);
	    }

	    if (request.getName() != null) {
	        product.setName(request.getName());
	    }

	    if (request.getPrice() != null) {
	        product.setPrice(request.getPrice());
	    }

	    if (request.getStockQuantity() != null) {
	        product.setStockQuantity(request.getStockQuantity());
	    }

	    if (request.getStatus() != null) {
	        product.setStatus(request.getStatus());
	    }
		 
	    if(request.getRam() != null){
	        product.setRam(request.getRam());
	    }

	    if(request.getStorage() != null){
	        product.setStorage(request.getStorage());
	    }

	    if(request.getBattery() != null){
	        product.setBattery(request.getBattery());
	    }

	    if(request.getScreenSize() != null){
	        product.setScreenSize(
	            request.getScreenSize()
	        );
	    }
	    
		return ProductMapper.toResponse(product);
	}
	
	@Transactional
	 public void uploadImages(Long productId, List<MultipartFile> files) {

	        Product product = findById(productId);

	        for (MultipartFile file : files) {

	        	String url = fileStorageService.save(file, FileStorageService.PRODUCTS);

	            ProductImage image = new ProductImage();
	            image.setProduct(product);
	            image.setImageUrl(url);

	            productImageRepository.save(image);
	        }
	 }

	@Transactional
	public void hideProduct(Long id) {
		Product product = findById(id);
		
		product.setStatus(ProductStatus.INACTIVE);
	}
	
	public Page<ProductResponse> getProducts(Pageable pageable, ProductFilter filter) {
		if(filter == null){
		    filter = ProductFilter
		    		.builder()
		    		.build();
		}
		
		Page<Product> products = productRepository.findAll(
								ProductSpecification.filter(filter),
								pageable
		      );
		 return products.map(ProductMapper::toResponse);
	}
	
	public ProductResponse getProductById(Long id){

	    Product product = findById(id);

	    if(
	        product.getStatus()
	        != ProductStatus.ACTIVE
	    ){

	        throw new ResourceNotFoundException(
	            "Product",
	            id
	        );

	    }

	    return ProductMapper.toResponse(product);
	}
}

	   



