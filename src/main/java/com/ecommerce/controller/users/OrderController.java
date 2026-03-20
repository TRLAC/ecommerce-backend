package com.ecommerce.controller.users;

import org.springframework.http.HttpStatus;
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
import com.ecommerce.dto.request.CancelOrderRequest;
import com.ecommerce.dto.request.PlaceOrderRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * OrderController — dành cho USER (customer).
 * Base path: /api/orders
 *
 * Use cases covered:
 *  - Place Order   (<<include>> Redirect To Payment, Deduct Stock)
 *  - View Order
 *  - Cancel Order  (<<extend>> Refund Payment nếu đã thanh toán)
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ----------------------------------------------------------------
    // UC: Place Order
    // POST /api/orders
    // ----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PlaceOrderRequest request) {

        OrderResponse response = orderService.placeOrder(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    // ----------------------------------------------------------------
    // UC: View Order (single)
    // GET /api/orders/{orderId}
    // ----------------------------------------------------------------
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> viewOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId) {

        OrderResponse response = orderService.viewOrder(orderId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ----------------------------------------------------------------
    // UC: View Orders (danh sách của chính user, có filter + paging)
    // GET /api/orders/my
    // ----------------------------------------------------------------
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrders(
            @RequestHeader("X-User-Id") Long userId,
            OrderFilter filter) {

        PageResponse<OrderResponse> response = orderService.getMyOrders(userId, filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ----------------------------------------------------------------
    // UC: Cancel Order
    // PATCH /api/orders/{orderId}/cancel
    // ----------------------------------------------------------------
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody(required = false) CancelOrderRequest request) {

        if (request == null) request = new CancelOrderRequest();
        OrderResponse response = orderService.cancelOrder(orderId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
    }
}