package com.ecommerce.dto.response;

import java.time.LocalDateTime;

import com.ecommerce.enums.ShippingStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShippingResponse {

    private Long shippingId;
    private Long orderId;
    private String address;
    private ShippingStatus shippingStatus;
    private String trackingCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}