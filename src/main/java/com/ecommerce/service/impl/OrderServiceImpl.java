package com.ecommerce.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderStatusLog;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.User;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderStatusLogRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.utils.OrderStatusTransition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class OrderServiceImpl implements OrderService {
	private final OrderStatusLogRepository orderStatusLogRepository;
	private final CartRepository cartRepository;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    
	@Override
	public OrderResponse createOrderFromCart(Long userId, PaymentMethod paymentMethod) {
		Cart cart = cartRepository.findByUserIdWithItems(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart not found for user id: " + userId));
		
		if(cart.getItems() == null || cart.getItems().isEmpty()) {
			throw new BadRequestException("Cart is empty, cannot create order");
		}
		
		List<OrderItem> orderItems = new ArrayList<>();
		for(CartItem cartItem : cart.getItems()) {
			OrderItem orderItem = OrderItem.builder()
					.product(cartItem.getProduct())
		            .quantity(cartItem.getQuantity())
		            .priceSnapshot(cartItem.getPriceSnapshot())
		            .build();
			orderItems.add(orderItem);
		}
		
		BigDecimal totalAmount = BigDecimal.ZERO;
		for(OrderItem item : orderItems) {
			BigDecimal lineTotal = item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()));
			totalAmount = totalAmount.add(lineTotal);
		}
		
		for(OrderItem item : orderItems) {
			productService.deductStock(item.getProduct().getId(), item.getQuantity());
			
		}
		
		
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
		
		Order order = Order.builder()
		        .user(user)
		        .totalAmount(totalAmount)
		        .status(OrderStatus.PENDING)
		        .orderItems(orderItems)
		        .build();
		
	
		for (OrderItem item : orderItems) {
			   item.setOrder(order);
		}
		
		Payment payment = Payment.builder()
				.order(order)
				.method(paymentMethod)
				.amount(totalAmount)
				.status(PaymentStatus.PENDING)
				.build();
		
		order.setPayment(payment);
		orderRepository.save(order);
		
		cart.getItems().clear();
		cartRepository.save(cart);
		
		return orderMapper.toOrderResponse(order);
	}

	@Override
	public Page<OrderResponse> getOrderByUser(Long userId, Pageable pageable) {
	    Page<Order> orders = orderRepository.findByUser_Id(userId, pageable);
	    return orders.map(orderMapper::toOrderResponse);
	}

	@Override
	public OrderResponse getOrderDetail(Long id, Long userId) {
		Order order = orderRepository.findByIdAndUser_Id(id,userId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
		return orderMapper.toOrderResponse(order);
	}

	@Override
	public OrderResponse cancelOrder(Long id, Long userId) {
		Order order = orderRepository.findByIdAndUser_Id(id, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
		 
		PaymentMethod method = order.getPayment().getMethod();
		if(!OrderStatusTransition.canTransition(method, order.getStatus(), OrderStatus.CANCELLED)) {
			throw new BadRequestException("Cannot cancel order with current status: " + order.getStatus());
		}
		
		restoreStockForOrder(order);
		
		OrderStatusLog statusLog = OrderStatusLog.builder()
				.order(order)
				.oldStatus(order.getStatus())
				.newStatus(OrderStatus.CANCELLED)
				.changedBy(order.getUser())
				.build();
		
		orderStatusLogRepository.save(statusLog);
		order.setStatus(OrderStatus.CANCELLED);
		orderRepository.save(order);
		return orderMapper.toOrderResponse(order);
	}

	@Override
	public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, Long adminId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
		PaymentMethod method = order.getPayment().getMethod();
		if(!OrderStatusTransition.canTransition(method, order.getStatus(), newStatus)) {
			throw new BadRequestException("Cannot change order status from " + order.getStatus() + " to " + newStatus);
		}
		
		User admin = userRepository.findById(adminId)
	            .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));
		
		OrderStatusLog statusLog = OrderStatusLog.builder()
	            .order(order)
	            .oldStatus(order.getStatus())
	            .newStatus(newStatus)
	            .changedBy(admin)
	            .build();
		
		if (newStatus == OrderStatus.CANCELLED ) {   // newStatus có phải CANCELLED không
		    restoreStockForOrder(order);
		}
		
		orderStatusLogRepository.save(statusLog);
		order.setStatus(newStatus);
		orderRepository.save(order);
		
		return orderMapper.toOrderResponse(order);
	}
	
	private void restoreStockForOrder(Order order) {
		for(OrderItem item : order.getOrderItems()) {
			productService.restoreStock(item.getProduct().getId(), item.getQuantity());
		}
	}
}