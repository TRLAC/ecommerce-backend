package com.ecommerce.service;

import com.ecommerce.dto.filter.OrderFilter;
import com.ecommerce.dto.request.CancelOrderRequest;
import com.ecommerce.dto.request.ConfirmPaymentRequest;
import com.ecommerce.dto.request.PlaceOrderRequest;
import com.ecommerce.dto.request.RefundRequest;
import com.ecommerce.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.dto.request.UpdateShippingRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResponse;

public interface OrderService {

    // USER use cases
    OrderResponse placeOrder(Long userId, PlaceOrderRequest request);

    OrderResponse viewOrder(Long orderId, Long userId);

    PageResponse<OrderResponse> getMyOrders(Long userId, OrderFilter filter);

    OrderResponse cancelOrder(Long orderId, Long userId, CancelOrderRequest request);

    // ADMIN use cases
    PageResponse<OrderResponse> getAllOrders(OrderFilter filter);

    OrderResponse updateOrderStatus(Long orderId, Long adminId, UpdateOrderStatusRequest request);

    OrderResponse manageOrder(Long orderId, Long adminId, UpdateOrderStatusRequest request);

    // SYSTEM (internal
    void markAsPaid(Long orderId);
}