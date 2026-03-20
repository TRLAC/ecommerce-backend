package com.ecommerce.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.entity.Category;
import com.ecommerce.enums.CategoryStatus;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	boolean existsByName(String name);
	List<Category> findByParentIsNullAndStatus(CategoryStatus  status);
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
    @Query("""
            SELECT c FROM Category c
            WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:status IS NULL OR c.status = :status)
            """)
        Page<Category> findAllWithFilter(
                @Param("name") String name,
                @Param("status") CategoryStatus status,
                @Param("parentId") Long parentId,
                Pageable pageable
        );
}