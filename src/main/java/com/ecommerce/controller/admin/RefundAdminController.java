package com.ecommerce.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.request.UpdateRefundStatusRequest;
import com.ecommerce.dto.response.RefundResponse;
import com.ecommerce.service.RefundService;
import com.ecommerce.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RefundAdminController {

    private final RefundService refundService;
    private final UserService userService;

    @PatchMapping("/{refundId}/status")
    public ResponseEntity<RefundResponse> updateRefundStatus(
            @PathVariable Long refundId,
            @Valid @RequestBody UpdateRefundStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminId = userService.findByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.ok(refundService.updateRefundStatus(refundId, adminId, request));
    }
}