package com.ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.entity.Product;
import com.ecommerce.enums.ProductStatus;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

	Page<Product> findByStatus(ProductStatus status, Pageable pageable);

	@Modifying
	@Query("""
			    UPDATE Product p
			    SET p.stockQuantity =
			        p.stockQuantity - :qty
			    WHERE p.id = :productId
			    AND p.stockQuantity >= :qty
			""")
	int deductStock(@Param("productId") Long productId,

			@Param("qty") int qty);
	

	@Modifying
	@Query("""
			    UPDATE Product p
			    SET p.stockQuantity =
			        p.stockQuantity + :qty
			    WHERE p.id = :productId
			""")
	void restoreStock(@Param("productId") Long productId,

			@Param("qty") int qty);

}
