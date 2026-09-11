package com.ecommerce.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.request.ConfirmPaymentRequest;
import com.ecommerce.dto.response.PaymentResponse;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {@Override
	public PaymentResponse createPayment(Long orderId, PaymentMethod method) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PaymentResponse confirmPayment(ConfirmPaymentRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PaymentResponse refundIfPaid(Long orderId, String reason) {
		// TODO Auto-generated method stub
		return null;
	}

}