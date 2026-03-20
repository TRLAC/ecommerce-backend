package com.ecommerce.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ecommerce.dto.response.CartItemResponse;
import com.ecommerce.dto.response.CartResponse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class CreateCartRequest {
	 @NotNull(message = "Product id is required")
	    private Long productId;

	 @NotNull(message = "Quantity is required")
	 @Min(value = 1, message = "Quantity must be at least 1")
	    private Integer quantity;
}
