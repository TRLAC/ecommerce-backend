package com.ecommerce.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartResponse {

	private Long cartId;
    private Long userId;
    private Long productId;

    private String productName;
    private Integer quantity;
    private BigDecimal price;

}