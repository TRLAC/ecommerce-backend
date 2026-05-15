package com.ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.request.ConfirmPaymentRequest;
import com.ecommerce.dto.response.PaymentResponse;
import com.ecommerce.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentCallbackController {

	private final PaymentService paymentService;

	@PostMapping("/confirm")
	public ResponseEntity<PaymentResponse> confirmPayment(
	        @Valid @RequestBody ConfirmPaymentRequest request) {

	    return ResponseEntity.ok(
	        paymentService.confirmPayment(request)
	    );
	}
      
}