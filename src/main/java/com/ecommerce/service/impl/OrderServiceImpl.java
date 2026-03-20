package com.ecommerce.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.exception.BadRequestException; 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.filter.OrderFilter;
import com.ecommerce.dto.request.CancelOrderRequest;
import com.ecommerce.dto.request.ConfirmPaymentRequest;
import com.ecommerce.dto.request.OrderItemRequest;
import com.ecommerce.dto.request.PlaceOrderRequest;
import com.ecommerce.dto.request.RefundRequest;
import com.ecommerce.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.dto.request.UpdateShippingRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderStatusLog;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Refund;
import com.ecommerce.entity.Shipping;
import com.ecommerce.entity.User;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.enums.RefundStatus;
import com.ecommerce.enums.ShippingStatus;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderSpecification;
import com.ecommerce.repository.OrderStatusLogRepository;
import com.ecommerce.repository.PaymentRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.RefundRepository;
import com.ecommerce.repository.ShippingRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final PaymentRepository paymentRepository;
    private final ShippingRepository shippingRepository;
    private final RefundRepository refundRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    // ----------------------------------------------------------------
    // USER: Place Order  →  <<include>> Redirect To Payment, Deduct Stock
    // ----------------------------------------------------------------
    @Override
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.getProductId()));

            // <<include>> Deduct Stock
            int updated = productRepository.deductStock(product.getProductId(), itemReq.getQuantity());
            if (updated == 0) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .priceSnapshot(product.getPrice())
                    .build();
            items.add(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        // Persist order
        Order order = Order.builder()
                .user(user)
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .build();
        order = orderRepository.save(order);

        // Associate items with order
        for (OrderItem item : items) {
            item.setOrder(order);
        }
        orderItemRepository.saveAll(items);
        order.setOrderItems(items);

        // <<include>> Redirect To Payment — tạo payment record ở trạng thái PENDING
        Payment payment = Payment.builder()
                .order(order)
                .method(request.getPaymentMethod())
                .amount(total)
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);
        order.setPayment(payment);

        // Tạo shipping record
        Shipping shipping = Shipping.builder()
                .order(order)
                .address(request.getShippingAddress())
                .shippingStatus(ShippingStatus.PENDING)
                .build();
        shippingRepository.save(shipping);
        order.setShipping(shipping);

        // Ghi log trạng thái ban đầu
        logStatusChange(order, null, OrderStatus.PENDING, user);

        log.info("Order placed: orderId={}, userId={}, total={}", order.getId(), userId, total);
        return orderMapper.toOrderResponse(order);
    }

    // ----------------------------------------------------------------
    // USER: View Order
    // ----------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public OrderResponse viewOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId + " for user: " + userId));
        return orderMapper.toOrderResponse(order);
    }

    // ----------------------------------------------------------------
    // USER: Danh sách đơn hàng của chính mình (paged + filtered)
    // ----------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(Long userId, OrderFilter filter) {
        filter.setUserId(userId);
        return buildPageResponse(filter);
    }

    // ----------------------------------------------------------------
    // USER: Cancel Order  ←  <<extend>> Refund Payment (nếu đã thanh toán)
    // ----------------------------------------------------------------
    @Override
    public OrderResponse cancelOrder(Long orderId, Long userId, CancelOrderRequest request) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId + " for user: " + userId));

        if (!isCancellable(order.getStatus())) {
            throw new BadRequestException("Cannot cancel order with status: " + order.getStatus());
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        logStatusChange(order, previousStatus, OrderStatus.CANCELLED, user);

        // <<extend>> Refund Payment — chỉ kích hoạt nếu đã thanh toán
        Payment payment = order.getPayment();
        if (payment != null && PaymentStatus.PAID.equals(payment.getStatus())) {
            initiateRefund(
                    payment,
                    payment.getAmount(),
                    request.getReason() != null ? request.getReason() : "Order cancelled by user"
            );
        }

        // Khôi phục tồn kho
        restoreOrderStock(order);

        log.info("Order cancelled: orderId={}, userId={}", orderId, userId);
        return orderMapper.toOrderResponse(
                orderRepository.findByIdWithDetails(orderId).orElse(order));
    }

    // ----------------------------------------------------------------
    // ADMIN: Lấy toàn bộ đơn hàng (paged + filtered)
    // ----------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(OrderFilter filter) {
        return buildPageResponse(filter);
    }

    // ----------------------------------------------------------------
    // ADMIN: Xem chi tiết 1 đơn (không giới hạn userId)
    // ----------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForAdmin(Long orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return orderMapper.toOrderResponse(order);
    }

    // ----------------------------------------------------------------
    // ADMIN: Update Order Status
    // ----------------------------------------------------------------
    @Override
    public OrderResponse updateOrderStatus(Long orderId, Long adminId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminId));

        validateStatusTransition(order.getStatus(), request.getNewStatus());

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(request.getNewStatus());
        orderRepository.save(order);

        logStatusChange(order, previousStatus, request.getNewStatus(), admin);

        log.info("Order status updated: orderId={}, {} -> {}", orderId, previousStatus, request.getNewStatus());
        return orderMapper.toOrderResponse(order);
    }

    // ----------------------------------------------------------------
    // ADMIN: Manage Order  <<include>> Update Order Status
    // ----------------------------------------------------------------
    @Override
    public OrderResponse manageOrder(Long orderId, Long adminId, UpdateOrderStatusRequest request) {
        return updateOrderStatus(orderId, adminId, request);
    }

    // ----------------------------------------------------------------
    // PAYMENT SYSTEM: Confirm Payment  <<include>> Update Order Status
    // ----------------------------------------------------------------
    @Override
    public OrderResponse confirmPayment(ConfirmPaymentRequest request) {
        Order order = orderRepository.findByIdWithDetails(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

        Payment payment = order.getPayment();
        if (payment == null) {
            throw new BadRequestException("No payment record found for order: " + request.getOrderId());
        }
        if (PaymentStatus.PAID.equals(payment.getStatus())) {
            throw new BadRequestException("Payment already confirmed for order: " + request.getOrderId());
        }

        // Cập nhật payment
        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId(request.getTransactionId());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // <<include>> Update Order Status → CONFIRMED
        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        logStatusChange(order, previousStatus, OrderStatus.CONFIRMED, null);

        log.info("Payment confirmed: orderId={}, transactionId={}",
                request.getOrderId(), request.getTransactionId());
        return orderMapper.toOrderResponse(order);
    }

    // ----------------------------------------------------------------
    // ADMIN: Refund Payment  <<extend>> Cancel Order
    // ----------------------------------------------------------------
    @Override
    public OrderResponse refundPayment(Long orderId, Long adminId, RefundRequest request) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        Payment payment = order.getPayment();
        if (payment == null || !PaymentStatus.PAID.equals(payment.getStatus())) {
            throw new BadRequestException("Order has no confirmed payment to refund");
        }
        if (refundRepository.existsByPayment_Order_IdAndStatus(orderId, RefundStatus.PENDING)
                || refundRepository.existsByPayment_Order_IdAndStatus(orderId, RefundStatus.APPROVED)) {
            throw new BadRequestException("A refund is already in progress for this order");
        }

        initiateRefund(payment, request.getAmount(), request.getReason());

        // Cập nhật trạng thái order
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminId));
        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);
        logStatusChange(order, previousStatus, OrderStatus.REFUNDED, admin);

        // Khôi phục tồn kho
        restoreOrderStock(order);

        log.info("Refund initiated: orderId={}, amount={}", orderId, request.getAmount());
        return orderMapper.toOrderResponse(
                orderRepository.findByIdWithDetails(orderId).orElse(order));
    }

    // ----------------------------------------------------------------
    // ADMIN: Update Shipping
    // ----------------------------------------------------------------
    @Override
    public OrderResponse updateShipping(Long orderId, Long adminId, UpdateShippingRequest request) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        Shipping shipping = order.getShipping();
        if (shipping == null) {
            throw new BadRequestException("No shipping record found for order: " + orderId);
        }

        shipping.setShippingStatus(request.getShippingStatus());
        if (request.getTrackingCode() != null && !request.getTrackingCode().isBlank()) {
            shipping.setTrackingCode(request.getTrackingCode());
        }
        shippingRepository.save(shipping);

        // Auto-sync order status khi shipping DELIVERED
        if (ShippingStatus.DELIVERED.equals(request.getShippingStatus())
                && !OrderStatus.DELIVERED.equals(order.getStatus())) {
            User admin = userRepository.findById(adminId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", adminId));
            OrderStatus previousStatus = order.getStatus();
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
            logStatusChange(order, previousStatus, OrderStatus.DELIVERED, admin);
        }

        log.info("Shipping updated: orderId={}, shippingStatus={}", orderId, request.getShippingStatus());
        return orderMapper.toOrderResponse(order);
    }

    // ================================================================
    // Private helpers
    // ================================================================

    /**
     * Khởi tạo Refund và cập nhật Payment → REFUNDED.
     * Không nhận Order vì chỉ cần Payment (payment đã chứa FK order_id).
     */
    private void initiateRefund(Payment payment, BigDecimal amount, String reason) {
        Refund refund = Refund.builder()
                .payment(payment)
                .amount(amount)
                .reason(reason)
                .status(RefundStatus.PENDING)
                .build();
        refundRepository.save(refund);

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefund(refund);
        paymentRepository.save(payment);
    }

    /** Khôi phục tồn kho cho tất cả items của order. */
    private void restoreOrderStock(Order order) {
        if (order.getOrderItems() == null) return;
        for (OrderItem item : order.getOrderItems()) {
            productRepository.restoreStock(
                    item.getProduct().getProductId(),
                    item.getQuantity());
        }
    }

    /** Ghi 1 dòng log chuyển trạng thái đơn hàng. */
    private void logStatusChange(Order order, OrderStatus oldStatus,
                                  OrderStatus newStatus, User changedBy) {
        OrderStatusLog statusLog = OrderStatusLog.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .build();
        orderStatusLogRepository.save(statusLog);
    }

    /** Chỉ cho phép cancel khi PENDING hoặc CONFIRMED. */
    private boolean isCancellable(OrderStatus status) {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    /**
     * State machine chuyển trạng thái hợp lệ:
     * PENDING → CONFIRMED | CANCELLED
     * CONFIRMED → PROCESSING | CANCELLED
     * PROCESSING → SHIPPING | CANCELLED
     * SHIPPING → DELIVERED
     * DELIVERED → REFUNDED
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING    -> next == OrderStatus.CONFIRMED  || next == OrderStatus.CANCELLED;
            case CONFIRMED  -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED;
            case PROCESSING -> next == OrderStatus.SHIPPED   || next == OrderStatus.CANCELLED;
            case SHIPPED   -> next == OrderStatus.DELIVERED;
            case DELIVERED  -> next == OrderStatus.REFUNDED;
            default         -> false;
        };
        if (!valid) {
            throw new BadRequestException(
                    "Invalid status transition: " + current + " -> " + next);
        }
    }

    /** Build PageResponse từ filter chung cho cả USER và ADMIN. */
    private PageResponse<OrderResponse> buildPageResponse(OrderFilter filter) {
        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<Order> page = orderRepository.findAll(
                OrderSpecification.withFilter(filter), pageable);

        List<OrderResponse> content = page.getContent().stream()
                .map(orderMapper::toOrderResponse)
                .toList();

        return PageResponse.<OrderResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}