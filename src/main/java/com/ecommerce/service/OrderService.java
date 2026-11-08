package com.ecommerce.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMethod;

public interface OrderService {
	OrderResponse createOrderFromCart(Long userId, PaymentMethod paymentMethod, String address);
	
	Page<OrderResponse> getOrderByUser(Long userId, Pageable pageable);
	
	OrderResponse getOrderDetail(Long id, Long userId);
	
	OrderResponse cancelOrder(Long id, Long userId);
	
	OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, Long adminId);
  
}