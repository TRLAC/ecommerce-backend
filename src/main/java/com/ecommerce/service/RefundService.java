package com.ecommerce.service;

import com.ecommerce.dto.request.RefundRequest;

public interface RefundService {
    void refund(RefundRequest request);
}