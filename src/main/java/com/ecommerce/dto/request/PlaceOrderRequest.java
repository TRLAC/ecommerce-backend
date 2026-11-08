package com.ecommerce.dto.request;

import com.ecommerce.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlaceOrderRequest {
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    // Không gửi thì dùng địa chỉ trong hồ sơ người dùng
    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;
}