package com.ecommerce.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.filter.OrderFilter;
import com.ecommerce.dto.request.*;
import com.ecommerce.dto.response.*;
import com.ecommerce.entity.*;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.repository.*;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.PaymentService;
import com.ecommerce.service.ShippingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    // ✅ dùng interface
    private final PaymentService paymentService;
    private final ShippingService shippingService;

    // ================================================================
    // USER: PLACE ORDER
    // ================================================================
    @Override
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {

        User user = getUser(userId);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.getProductId()));

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .priceSnapshot(product.getPrice())
                    .build();

            items.add(item);

            total = total.add(product.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        Order order = Order.builder()
                .user(user)
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .build();

        orderRepository.save(order);

        items.forEach(i -> i.setOrder(order));
        orderItemRepository.saveAll(items);

        logStatusChange(order, null, OrderStatus.PENDING, user);

        // 👉 chỉ trigger
        paymentService.createPayment(order.getId(), request.getPaymentMethod());
        shippingService.createShipping(order.getId(), request.getShippingAddress());

        return orderMapper.toOrderResponse(order);
    }

    // ================================================================
    // USER: VIEW ORDER
    // ================================================================
    @Override
    @Transactional(readOnly = true)
    public OrderResponse viewOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        return orderMapper.toOrderResponse(order);
    }

    // ================================================================
    // USER: MY ORDERS
    // ================================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(Long userId, OrderFilter filter) {
        filter.setUserId(userId);
        return buildPageResponse(filter);
    }

    // ================================================================
    // USER: CANCEL ORDER
    // ================================================================
    @Override
    public OrderResponse cancelOrder(Long orderId, Long userId, CancelOrderRequest request) {

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!isCancellable(order.getStatus())) {
            throw new BadRequestException("Cannot cancel order");
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);

        User user = getUser(userId);

        logStatusChange(order, previousStatus, OrderStatus.CANCELLED, user);

        // 👉 refund nếu đã thanh toán
        paymentService.refundIfPaid(orderId, request.getReason());

        return orderMapper.toOrderResponse(order);
    }

    // ================================================================
    // ADMIN: UPDATE STATUS
    // ================================================================
    @Override
    public OrderResponse updateOrderStatus(Long orderId, Long adminId, UpdateOrderStatusRequest request) {

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        User admin = getUser(adminId);

        validateStatusTransition(order.getStatus(), request.getNewStatus());

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(request.getNewStatus());

        logStatusChange(order, previousStatus, request.getNewStatus(), admin);

        return orderMapper.toOrderResponse(order);
    }

    // ================================================================
    // SYSTEM: PAYMENT SUCCESS
    // ================================================================
    @Override
    public void markAsPaid(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Invalid order state");
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CONFIRMED);

        logStatusChange(order, previousStatus, OrderStatus.CONFIRMED, null);
    }

    // ================================================================
    // HELPERS
    // ================================================================
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private void logStatusChange(Order order, OrderStatus oldStatus,
                                OrderStatus newStatus, User changedBy) {

        OrderStatusLog logEntry = OrderStatusLog.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .build();

        orderStatusLogRepository.save(logEntry);
    }

    private boolean isCancellable(OrderStatus status) {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING    -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED  -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED;
            case PROCESSING -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED    -> next == OrderStatus.DELIVERED;
            case DELIVERED  -> next == OrderStatus.REFUNDED;
            default         -> false;
        };

        if (!valid) {
            throw new BadRequestException("Invalid status transition: " + current + " -> " + next);
        }
    }

    private PageResponse<OrderResponse> buildPageResponse(OrderFilter filter) {

        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<Order> page = orderRepository.findAll(
                OrderSpecification.withFilter(filter), pageable);

        return PageResponse.<OrderResponse>builder()
                .content(page.getContent().stream()
                        .map(orderMapper::toOrderResponse)
                        .toList())
                .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .last(page.isLast())
                        .build();
    }

	@Override
	public PageResponse<OrderResponse> getAllOrders(OrderFilter filter) {
	    return buildPageResponse(filter);
	}

	@Override
	public OrderResponse manageOrder(Long orderId, Long adminId, UpdateOrderStatusRequest request) {
	    return updateOrderStatus(orderId, adminId, request);
	}
}