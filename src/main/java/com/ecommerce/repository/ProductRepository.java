package com.ecommerce.repository;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.entity.Product;
import com.ecommerce.enums.ProductStatus;

public interface ProductRepository extends JpaRepository<Product, Long> {
	@Query("""
			SELECT p FROM Product p
			WHERE p.status = :status
			AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
			AND (:minPrice IS NULL OR p.price >= :minPrice)
			AND (:maxPrice IS NULL OR p.price <= :maxPrice)
			AND (:categoryId IS NULL OR p.category.categoryId = :categoryId)
			""")
			Page<Product> searchProducts(
			        @Param("keyword") String keyword,
			        @Param("minPrice") BigDecimal  minPrice,
			        @Param("maxPrice") BigDecimal  maxPrice,
			        @Param("categoryId") Long categoryId,
			        @Param("status") ProductStatus status,
			        Pageable pageable
			);
	Page<Product> findByStatus(ProductStatus status, Pageable pageable);
}
