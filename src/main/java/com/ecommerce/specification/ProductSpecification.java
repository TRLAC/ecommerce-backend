package com.ecommerce.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ecommerce.dto.filter.ProductFilter;
import com.ecommerce.entity.Product;
import com.ecommerce.enums.ProductStatus;

import jakarta.persistence.criteria.Predicate;

public class ProductSpecification {

	public static Specification<Product> filter(ProductFilter filter) {

		return (root, query, cb) -> {

			List<Predicate> predicates = new ArrayList<>();
			
			predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));

			if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {

				predicates
						.add(cb.like(cb.lower(root.get("name")), "%" + filter.getKeyword().trim().toLowerCase() + "%"));

			}

			if (filter.getMinPrice() != null) {

				predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));

			}

			if (filter.getMaxPrice() != null) {

				predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));

			}

			if (filter.getCategoryId() != null) {
			    predicates.add(
			        cb.or(
			            // Đúng category đó
			            cb.equal(root.get("category").get("categoryId"), filter.getCategoryId()),
			            // Hoặc con của category đó
			            cb.equal(root.get("category").get("parent").get("categoryId"), filter.getCategoryId())
			        )
			    );
			}

			if (filter.getBrandId() != null) {

				predicates.add(cb.equal(root.get("brand").get("brandId"), filter.getBrandId()));

			}

			if (filter.getRam() != null) {

				predicates.add(cb.equal(root.get("ram"), filter.getRam()));

			}

			if (filter.getStorage() != null) {

				predicates.add(cb.equal(root.get("storage"), filter.getStorage()));

			}

			return cb.and(predicates.toArray(new Predicate[0]));

		};

	}

}