package com.ecommerce.controller.users;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.request.RefundRequest;
import com.ecommerce.dto.response.RefundResponse;
import com.ecommerce.service.RefundService;
import com.ecommerce.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final UserService userService;

    @PostMapping("/orders/{orderId}")
    public ResponseEntity<RefundResponse> requestRefund(
            @PathVariable Long orderId,
            @Valid @RequestBody RefundRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.findByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(refundService.requestRefund(orderId, userId, request));
    }
}