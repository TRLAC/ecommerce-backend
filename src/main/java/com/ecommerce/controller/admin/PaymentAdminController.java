package com.ecommerce.controller.admin;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.response.PaymentResponse;
import com.ecommerce.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PaymentAdminController {

    private final PaymentService paymentService;

    // Refund payment
    @PostMapping("/refund/{orderId}")
    public PaymentResponse refund(@PathVariable Long orderId,
                                  @RequestParam String reason) {
        return paymentService.refundIfPaid(orderId, reason);
    }
}
