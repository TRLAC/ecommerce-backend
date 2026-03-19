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
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.repository.BrandRepository;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductImageRepository;
import com.ecommerce.repository.ProductRepository;


@Service
public class ProductService {
	private final ProductRepository productRepository;
	private final BrandRepository brandRepository;
	private final CategoryRepository categoryRepository;
	private final FileStorageService fileStorageService;
	private final ProductImageRepository productImageRepository;
	
	public ProductService(
			ProductRepository productRepository, 
			BrandRepository brandRepository, 
			CategoryRepository categoryRepository, 
			ProductImageRepository productImageRepository, 
			FileStorageService fileStorageService) {
		this.productRepository = productRepository;
		this.brandRepository = brandRepository;
		this.categoryRepository = categoryRepository;
		this.productImageRepository = productImageRepository;
		this.fileStorageService = fileStorageService;
	}
	
	public Page<ProductResponse> getAllProducts(Pageable pageable) {
	    return productRepository
	    		.findByStatus(ProductStatus.ACTIVE, pageable)
	            .map(ProductMapper::toResponse);
	}
	
	public ProductResponse getProductById(Long id) {
		Product product = productRepository.findById(id)
				.filter(p -> p.getStatus() == ProductStatus.ACTIVE)
				.orElseThrow(() -> new RuntimeException("Product not found"));
		return ProductMapper.toResponse(product);
	}
	
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
	
	public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
		
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found"));
		
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
		 
		    product.setName(request.getName());
		    product.setPrice(request.getPrice());
		    product.setStockQuantity(request.getStockQuantity());
		    product.setStatus(request.getStatus());

		    product.setRam(request.getRam());
		    product.setStorage(request.getStorage());
		    product.setBattery(request.getBattery());
		    product.setScreenSize(request.getScreenSize());

		    productRepository.save(product);
		return ProductMapper.toResponse(product);
	}
	
	 public void uploadImages(Long productId, List<MultipartFile> files) {

	        Product product = productRepository.findById(productId)
	                .orElseThrow(() -> new RuntimeException("Product not found"));

	        for (MultipartFile file : files) {

	            String url = fileStorageService.save(file);

	            ProductImage image = new ProductImage();
	            image.setProduct(product);
	            image.setImageUrl(url);

	            productImageRepository.save(image);
	        }
	 }

	public void hideProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found"));
		
		product.setStatus(ProductStatus.INACTIVE);
		
		productRepository.save(product);
	}
	
	public Page<ProductResponse> getProducts(Pageable pageable, ProductFilter filter) {
		if(filter == null){
		    filter = new ProductFilter();
		}
		Page<Product> products = productRepository.searchProducts(
				 filter.getKeyword(),
		            filter.getMinPrice(),
		            filter.getMaxPrice(),
		            filter.getCategoryId(),
		            ProductStatus.ACTIVE,
		            pageable
		      );
		 return products.map(ProductMapper::toResponse);
	}
}

	   



