package com.ecommerce.service;

import com.ecommerce.dto.request.ConfirmPaymentRequest;
import com.ecommerce.dto.response.PaymentResponse;
import com.ecommerce.enums.PaymentMethod;

public interface PaymentService {
    PaymentResponse createPayment(Long orderId, Long userId, PaymentMethod method);
    PaymentResponse confirmPayment(Long userId, ConfirmPaymentRequest request);
}