package com.ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.request.ConfirmPaymentRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentCallbackController {

    private final OrderService orderService;

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmPayment(
            @Valid @RequestBody ConfirmPaymentRequest request) {

        OrderResponse response = orderService.confirmPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment confirmed successfully", response));
    }
}