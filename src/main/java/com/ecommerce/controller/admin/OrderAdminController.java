package com.ecommerce.controller.admin;

import com.ecommerce.dto.filter.OrderFilter;
import com.ecommerce.dto.request.RefundRequest;
import com.ecommerce.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.dto.request.UpdateShippingRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(OrderFilter filter) {
        return ResponseEntity.ok(orderService.getAllOrders(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderDetail(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderByIdForAdmin(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(orderService.updateOrderStatus(id, adminId, request));
    }

    @PatchMapping("/{id}/manage")
    public ResponseEntity<OrderResponse> manageOrder(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(orderService.manageOrder(id, adminId, request));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<OrderResponse> refundPayment(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(orderService.refundPayment(id, adminId, request));
    }

    @PatchMapping("/{id}/shipping")
    public ResponseEntity<OrderResponse> updateShipping(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShippingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(orderService.updateShipping(id, adminId, request));
    }

    // JWT username = email → dùng UserService.findByEmail() để lấy id
    private Long getCurrentUserId(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername()).getId();
    }
}