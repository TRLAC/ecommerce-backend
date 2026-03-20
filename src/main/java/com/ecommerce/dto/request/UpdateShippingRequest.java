package com.ecommerce.dto.request;

import com.ecommerce.enums.ShippingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateShippingRequest {

    @NotNull(message = "Shipping status is required")
    private ShippingStatus shippingStatus;

    private String trackingCode;
}