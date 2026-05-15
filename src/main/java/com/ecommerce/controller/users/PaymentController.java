package com.ecommerce.controller.users;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.request.ConfirmPaymentRequest;
import com.ecommerce.dto.response.PaymentResponse;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class PaymentController {

	 private final PaymentService paymentService;

	    // Tạo payment cho order
	    @PostMapping("/{orderId}")
	    public PaymentResponse createPayment(@PathVariable Long orderId,
	                                         @RequestParam PaymentMethod method) {
	        return paymentService.createPayment(orderId, method);
	    }

	    // Xác nhận thanh toán
	    @PostMapping("/confirm")
	    public PaymentResponse confirmPayment(@RequestBody ConfirmPaymentRequest request) {
	        return paymentService.confirmPayment(request);
	    }
}
