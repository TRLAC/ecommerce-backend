package com.ecommerce.dto.request;

import java.util.List;

import com.ecommerce.enums.PaymentMethod;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {
	@NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;
 
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
 
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
