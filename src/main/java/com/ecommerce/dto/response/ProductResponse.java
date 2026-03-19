package com.ecommerce.dto.response;
import java.math.BigDecimal;
import java.util.List;

import com.ecommerce.enums.ProductStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponse {

    private Long productId;

    private String name;

    private BigDecimal price;

    private Integer stockQuantity;

    private ProductStatus status;

    private Integer ram;
    private Integer storage;
    private Integer battery;
    private Double screenSize;

    private List<String> images;
    
    private String brandName;
    private String categoryName;
}