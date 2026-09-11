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
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
