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

import com.ecommerce.dto.request.UpdateShippingRequest;
import com.ecommerce.dto.response.ShippingResponse;
import com.ecommerce.service.ShippingService;
import com.ecommerce.service.impl.UserServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ShippingAdminController {

    private final ShippingService shippingService;
    private final UserServiceImpl userService;

    @PatchMapping("/{orderId}/shipping")
    public ResponseEntity<ShippingResponse> updateShipping(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateShippingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminId = userService.findByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.ok(shippingService.updateShipping(orderId, adminId, request));
    }
}