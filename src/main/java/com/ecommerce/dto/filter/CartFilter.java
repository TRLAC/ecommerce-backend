package com.ecommerce.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartFilter {
 
    private Long userId;
    private Long productId;
    private Integer minQuantity;
    private Integer maxQuantity;
}