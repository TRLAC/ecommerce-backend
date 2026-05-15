package com.ecommerce.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.filter.OrderFilter;
import com.ecommerce.dto.request.CancelOrderRequest;
import com.ecommerce.dto.request.OrderItemRequest;
import com.ecommerce.dto.request.PlaceOrderRequest;
import com.ecommerce.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderStatusLog;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderSpecification;
import com.ecommerce.repository.OrderStatusLogRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;

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
    private final ProductService productService;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
  
    @Override
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
    	log.info("Placing order for user: {}", userId);
    	
    	if(request.getItems() == null || request.getItems().isEmpty()) {
    		throw new BadRequestException("Order must contain at least one item");
    	}
    	
    	User user = getUser(userId);
    	List<OrderItem> items = new ArrayList<>();
    	BigDecimal total = BigDecimal.ZERO;
    	
    	for(OrderItemRequest itemReq : request.getItems()) {
    		Product product = productService.findById(itemReq.getProductId());
    		
    		  productService.deductStock(product.getProductId(), itemReq.getQuantity());

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
    	log.info("Order created with ID: {}", order.getId());
    	
    	// Save order items
        items.forEach(i -> i.setOrder(order));
        orderItemRepository.saveAll(items);

        // Log status change
        logStatusChange(order, null, OrderStatus.PENDING, user);

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
        
        for (OrderItem item : order.getOrderItems()) {
            productService.restoreStock(
                    item.getProduct().getProductId(),
                    item.getQuantity()
            );
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);

        User user = getUser(userId);

        logStatusChange(order, previousStatus, OrderStatus.CANCELLED, user);

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
	        
	        orderRepository.save(order);
	    }
	 
	 @Override
	 public void markAsRefunded(Long orderId) {

		    Order order = orderRepository.findById(orderId)
		            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

		    OrderStatus oldStatus = order.getStatus();
		    order.setStatus(OrderStatus.REFUNDED);

		    logStatusChange(order, oldStatus, OrderStatus.REFUNDED, null);

		    orderRepository.save(order);
		}

	@Override
	public void markAsShipped(Long orderId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Transactional(readOnly = true)
	public OrderResponse getOrderByIdForAdmin(Long orderId) {

	    Order order = orderRepository.findByIdWithDetails(orderId)
	            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

	    return orderMapper.toOrderResponse(order);
	}

	
}