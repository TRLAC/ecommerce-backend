package com.ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.Brand;
import com.ecommerce.entity.Product;
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
	 boolean existsByBrandNameIgnoreCase(String brandName);
	 @EntityGraph(attributePaths = {"brand", "category"})
	 Page<Product> findAll(Specification<Product> spec, Pageable pageable);
}
