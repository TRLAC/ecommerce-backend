package com.ecommerce.dto.response;

import com.ecommerce.enums.ShippingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShippingResponse {

    private Long shippingId;
    private String address;
    private ShippingStatus shippingStatus;
    private String trackingCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}