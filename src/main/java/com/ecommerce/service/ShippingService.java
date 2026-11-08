package com.ecommerce.service;

import com.ecommerce.dto.request.UpdateShippingRequest;
import com.ecommerce.dto.response.ShippingResponse;

public interface ShippingService {
    ShippingResponse updateShipping(Long orderId, Long adminId, UpdateShippingRequest request);
}