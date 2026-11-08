package com.ecommerce.service;

import com.ecommerce.dto.request.RefundRequest;
import com.ecommerce.dto.request.UpdateRefundStatusRequest;
import com.ecommerce.dto.response.RefundResponse;

public interface RefundService {
    RefundResponse requestRefund(Long orderId, Long userId, RefundRequest request);
    RefundResponse updateRefundStatus(Long refundId, Long adminId, UpdateRefundStatusRequest request);
}