package com.ecommerce.controller.users;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
import com.ecommerce.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    // Tạo (hoặc thử lại) thanh toán cho đơn
    @PostMapping("/{orderId}")
    public PaymentResponse createPayment(@PathVariable Long orderId,
                                         @RequestParam PaymentMethod method,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        return paymentService.createPayment(orderId, getCurrentUserId(userDetails), method);
    }

    // Xác nhận kết quả từ cổng giả lập
    @PostMapping("/confirm")
    public PaymentResponse confirmPayment(@Valid @RequestBody ConfirmPaymentRequest request,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        return paymentService.confirmPayment(getCurrentUserId(userDetails), request);
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername()).getId();
    }
}