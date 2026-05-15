package com.ecommerce.service;

import java.util.Optional;

import com.ecommerce.dto.request.ConfirmPaymentRequest;
import com.ecommerce.dto.response.PaymentResponse;
import com.ecommerce.entity.Payment;
import com.ecommerce.enums.PaymentMethod;

public interface PaymentService {
	PaymentResponse createPayment(Long orderId, PaymentMethod method);
    PaymentResponse confirmPayment(ConfirmPaymentRequest request);
    PaymentResponse refundIfPaid(Long orderId, String reason);
}