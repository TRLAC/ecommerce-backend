package com.ecommerce.controller.admin;

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

    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(OrderFilter filter) {
        PageResponse<OrderResponse> response = orderService.getAllOrders(filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderDetail(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderByIdForAdmin(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminId = extractUserId(userDetails);
        OrderResponse response = orderService.updateOrderStatus(id, adminId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/manage")
    public ResponseEntity<OrderResponse> manageOrder(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminId = extractUserId(userDetails);
        OrderResponse response = orderService.manageOrder(id, adminId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<OrderResponse> refundPayment(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminId = extractUserId(userDetails);
        OrderResponse response = orderService.refundPayment(id, adminId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/shipping")
    public ResponseEntity<OrderResponse> updateShipping(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShippingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminId = extractUserId(userDetails);
        OrderResponse response = orderService.updateShipping(id, adminId, request);
        return ResponseEntity.ok(response);
    }

    // Lấy userId từ JWT principal — điều chỉnh theo cách project lưu userId trong UserDetails
    private Long extractUserId(UserDetails userDetails) {
        // Nếu UserDetails là custom class có getId() thì cast trực tiếp
        // Tạm dùng username nếu username là userId (string)
        try {
            return Long.parseLong(userDetails.getUsername());
        } catch (NumberFormatException e) {
            // Nếu username là email thì cần inject UserRepository để lookup
            // Trả về 0L tạm thời — xem ghi chú bên dưới
            return 0L;
        }
    }
}