package com.ecommerce.service;

import com.ecommerce.dto.request.ConfirmPaymentRequest;
import com.ecommerce.enums.PaymentMethod;

public interface PaymentService {
    void createPayment(Long orderId, PaymentMethod method);
    void confirmPayment(ConfirmPaymentRequest request);
	void refundIfPaid(Long orderId, String reason);
}