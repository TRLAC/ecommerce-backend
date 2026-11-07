package com.ecommerce.mapper;

import org.springframework.stereotype.Component;

import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.entity.Order;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderMapper {
	
	  private final OrderItemMapper orderItemMapper;
	    private final PaymentMapper paymentMapper;
	    private final ShippingMapper shippingMapper;
	    private final OrderStatusLogMapper orderStatusLogMapper;

    public OrderResponse toOrderResponse(Order order) {
        if (order == null) return null;

        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userFullName(order.getUser() != null ? order.getUser().getFullName() : null)
                .userEmail(order.getUser() != null ? order.getUser().getEmail() : null)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                
                .items(orderItemMapper.toOrderItemResponseList(
                        order.getOrderItems()))
                .payment(paymentMapper.toPaymentResponse(
                        order.getPayment()))

                .shipping(shippingMapper.toShippingResponse(
                        order.getShipping()))

                .statusLogs(orderStatusLogMapper.toStatusLogResponseList(
                        order.getStatusLogs()))
                .build();
    }
}