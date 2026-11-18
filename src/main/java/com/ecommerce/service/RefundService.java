package com.ecommerce.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.dto.request.RefundRequest;
import com.ecommerce.dto.request.UpdateRefundStatusRequest;
import com.ecommerce.dto.response.AdminRefundResponse;
import com.ecommerce.dto.response.RefundResponse;
import com.ecommerce.entity.Payment;
import com.ecommerce.enums.RefundStatus;

public interface RefundService {
	void createRefundForReturn(Payment payment);
    RefundResponse requestRefund(Long orderId, Long userId, RefundRequest request);
    RefundResponse updateRefundStatus(Long refundId, Long adminId, UpdateRefundStatusRequest request);

    // ADMIN: danh sách hoàn tiền, status = null thì lấy tất cả
    Page<AdminRefundResponse> getRefunds(RefundStatus status, Pageable pageable);
}