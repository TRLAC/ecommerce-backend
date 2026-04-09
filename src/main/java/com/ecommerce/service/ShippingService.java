package com.ecommerce.service;

import com.ecommerce.dto.request.UpdateShippingRequest;

public interface ShippingService {
    void createShipping(Long orderId, String address);
    void updateShipping(UpdateShippingRequest request);
}