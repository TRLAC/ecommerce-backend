package com.ecommerce.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderStatusLog;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.Shipping;
import com.ecommerce.entity.User;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.enums.ShippingStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderStatusLogRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;
import com.ecommerce.utils.OrderStatusTransition;
import com.ecommerce.utils.PaymentStatusTransition;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
	private final OrderStatusLogRepository orderStatusLogRepository;
	private final CartRepository cartRepository;
	private final ProductServiceImpl productService;
	private final UserRepository userRepository;
	private final OrderRepository orderRepository;
	private final OrderMapper orderMapper;

	@Override
	public OrderResponse createOrderFromCart(Long userId, PaymentMethod paymentMethod, String address) {
		Cart cart = cartRepository.findByUserIdWithItems(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart not found for user id: " + userId));

		if (cart.getItems() == null || cart.getItems().isEmpty()) {
			throw new BadRequestException("Cart is empty, cannot create order");
		}

		List<OrderItem> orderItems = new ArrayList<>();
		for (CartItem cartItem : cart.getItems()) {
			OrderItem orderItem = OrderItem.builder().product(cartItem.getProduct()).quantity(cartItem.getQuantity())
					.priceSnapshot(cartItem.getPriceSnapshot()).build();
			orderItems.add(orderItem);
		}

		BigDecimal totalAmount = BigDecimal.ZERO;
		for (OrderItem item : orderItems) {
			BigDecimal lineTotal = item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()));
			totalAmount = totalAmount.add(lineTotal);
		}

		for (OrderItem item : orderItems) {
			productService.deductStock(item.getProduct().getId(), item.getQuantity());

		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		String shippingAddress = (address != null && !address.isBlank()) ? address.trim() : user.getAddress();
		if (shippingAddress == null || shippingAddress.isBlank()) {
			throw new BadRequestException("Shipping address is required");
		}

		Order order = Order.builder().user(user).totalAmount(totalAmount).status(OrderStatus.PENDING)
				.orderItems(orderItems).build();

		for (OrderItem item : orderItems) {
			item.setOrder(order);
		}

		Payment payment = Payment.builder().order(order).method(paymentMethod).amount(totalAmount)
				.status(PaymentStatus.PENDING).build();

		order.addPayment(payment);

		Shipping shipping = Shipping.builder().order(order).address(shippingAddress)
				.shippingStatus(ShippingStatus.PENDING).build();
		order.setShipping(shipping);

		OrderStatusLog initialLog = OrderStatusLog.builder().order(order).newStatus(OrderStatus.PENDING).changedBy(user)
				.build();
		order.setStatusLogs(new ArrayList<>(List.of(initialLog)));

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
		Order order = orderRepository.findByIdAndUser_Id(id, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
		return orderMapper.toOrderResponse(order);
	}

	@Override
	public OrderResponse cancelOrder(Long id, Long userId) {
		Order order = orderRepository.findByIdAndUser_Id(id, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

		PaymentMethod method = order.getLatestPayment().getMethod();
		if (!OrderStatusTransition.canTransition(method, order.getStatus(), OrderStatus.CANCELLED)) {
			throw new BadRequestException("Cannot cancel order with current status: " + order.getStatus());
		}

		restoreStockForOrder(order);
		cancelPendingPayments(order);

		OrderStatusLog statusLog = OrderStatusLog.builder().order(order).oldStatus(order.getStatus())
				.newStatus(OrderStatus.CANCELLED).changedBy(order.getUser()).build();

		orderStatusLogRepository.save(statusLog);
		order.setStatus(OrderStatus.CANCELLED);
		orderRepository.save(order);
		return orderMapper.toOrderResponse(order);
	}

	@Override
	public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, Long adminId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
		PaymentMethod method = order.getLatestPayment().getMethod();
		if (!OrderStatusTransition.canTransition(method, order.getStatus(), newStatus)) {
			throw new BadRequestException("Cannot change order status from " + order.getStatus() + " to " + newStatus);
		}
		
		if (order.getShipping() != null
		        && (newStatus == OrderStatus.PROCESSING
		            || newStatus == OrderStatus.SHIPPED
		            || newStatus == OrderStatus.DELIVERED)) {
		    throw new BadRequestException("Use the shipping endpoint to move this order to " + newStatus);
		}

		if (newStatus == OrderStatus.PAID) {
			if (method != PaymentMethod.COD) {
				throw new BadRequestException("Online orders become PAID only through payment confirmation");
			}

			// COD: khách trả tiền mặt khi nhận hàng -> payment thành công
			Payment payment = order.getLatestPayment();
			if (PaymentStatusTransition.canTransition(payment.getStatus(), PaymentStatus.SUCCESS)) {
				payment.setStatus(PaymentStatus.SUCCESS);
				payment.setPaidAt(LocalDateTime.now());
			}
		}

		User admin = userRepository.findById(adminId)
				.orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));

		OrderStatusLog statusLog = OrderStatusLog.builder().order(order).oldStatus(order.getStatus())
				.newStatus(newStatus).changedBy(admin).build();

		if (newStatus == OrderStatus.CANCELLED) {
			restoreStockForOrder(order);
			cancelPendingPayments(order);
		}

		orderStatusLogRepository.save(statusLog);
		order.setStatus(newStatus);
		orderRepository.save(order);

		return orderMapper.toOrderResponse(order);
	}

	private void restoreStockForOrder(Order order) {
		for (OrderItem item : order.getOrderItems()) {
			productService.restoreStock(item.getProduct().getId(), item.getQuantity());
		}
	}

	private void cancelPendingPayments(Order order) {
		for (Payment payment : order.getPayments()) {
			if (PaymentStatusTransition.canTransition(payment.getStatus(), PaymentStatus.CANCELLED)) {
				payment.setStatus(PaymentStatus.CANCELLED);
			}
		}
	}
}