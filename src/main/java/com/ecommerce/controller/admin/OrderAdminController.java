package com.ecommerce.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.filter.OrderFilter;
import com.ecommerce.dto.request.RefundRequest;
import com.ecommerce.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.dto.request.UpdateShippingRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * OrderAdminController — dành cho ADMIN.
 * Base path: /api/admin/orders
 *
 * Use cases covered:
 *  - Manage Order        (<<include>> Update Order Status)
 *  - Update Order Status (state machine: PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED)
 *  - Refund Payment      (<<extend>> Cancel Order — admin chủ động hoàn tiền)
 *  - Update Shipping     (cập nhật trạng thái vận chuyển + tracking code)
 *  - Get all orders      (xem toàn bộ đơn hàng, filter + paging)
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderService orderService;

    // ----------------------------------------------------------------
    // UC: Manage Order — xem toàn bộ đơn hàng
    // GET /api/admin/orders
    // ----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            OrderFilter filter) {

        PageResponse<OrderResponse> response = orderService.getAllOrders(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ----------------------------------------------------------------
    // UC: Manage Order — xem chi tiết 1 đơn
    // GET /api/admin/orders/{orderId}
    // ----------------------------------------------------------------
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            @PathVariable Long orderId,
            @RequestHeader("X-Admin-Id") Long adminId) {

        // Reuse viewOrder — admin không bị giới hạn theo userId
        OrderResponse response = orderService.getOrderByIdForAdmin(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ----------------------------------------------------------------
    // UC: Manage Order  <<include>> Update Order Status
    // PATCH /api/admin/orders/{orderId}/manage
    // ----------------------------------------------------------------
    @PatchMapping("/{orderId}/manage")
    public ResponseEntity<ApiResponse<OrderResponse>> manageOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-Admin-Id") Long adminId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderResponse response = orderService.manageOrder(orderId, adminId, request);
        return ResponseEntity.ok(ApiResponse.success("Order managed successfully", response));
    }

    // ----------------------------------------------------------------
    // UC: Update Order Status (standalone)
    // PATCH /api/admin/orders/{orderId}/status
    // ----------------------------------------------------------------
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestHeader("X-Admin-Id") Long adminId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderResponse response = orderService.updateOrderStatus(orderId, adminId, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", response));
    }

    // ----------------------------------------------------------------
    // UC: Refund Payment (Admin-initiated)
    // POST /api/admin/orders/{orderId}/refund
    // ----------------------------------------------------------------
    @PostMapping("/{orderId}/refund")
    public ResponseEntity<ApiResponse<OrderResponse>> refundPayment(
            @PathVariable Long orderId,
            @RequestHeader("X-Admin-Id") Long adminId,
            @Valid @RequestBody RefundRequest request) {

        OrderResponse response = orderService.refundPayment(orderId, adminId, request);
        return ResponseEntity.ok(ApiResponse.success("Refund initiated successfully", response));
    }

    // ----------------------------------------------------------------
    // UC: Update Shipping
    // PATCH /api/admin/orders/{orderId}/shipping
    // ----------------------------------------------------------------
    @PatchMapping("/{orderId}/shipping")
    public ResponseEntity<ApiResponse<OrderResponse>> updateShipping(
            @PathVariable Long orderId,
            @RequestHeader("X-Admin-Id") Long adminId,
            @Valid @RequestBody UpdateShippingRequest request) {

        OrderResponse response = orderService.updateShipping(orderId, adminId, request);
        return ResponseEntity.ok(ApiResponse.success("Shipping updated successfully", response));
    }
}