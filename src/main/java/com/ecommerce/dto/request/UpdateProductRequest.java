package com.ecommerce.dto.request;

import java.math.BigDecimal;

import com.ecommerce.enums.ProductStatus;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductRequest {

    private Long brandId;

    private Long categoryId;

    private String name;

    @Min(value = 1, message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stockQuantity;

    private ProductStatus status;

    private Integer ram;
    private Integer storage;
    private Integer battery;
    private Double screenSize;
}