package com.ecommerce.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.request.UpdateShippingRequest;
import com.ecommerce.dto.response.ShippingResponse;
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
import com.ecommerce.mapper.ShippingMapper;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderStatusLogRepository;
import com.ecommerce.repository.ShippingRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.RefundService;
import com.ecommerce.service.ShippingService;
import com.ecommerce.utils.OrderStatusTransition;
import com.ecommerce.utils.PaymentStatusTransition;
import com.ecommerce.utils.ShippingStatusTransition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShippingServiceImpl implements ShippingService {

    private final ShippingRepository shippingRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final UserRepository userRepository;
    private final ShippingMapper shippingMapper;
    private final ProductServiceImpl productService;
    private final RefundService refundService;

    @Override
    public ShippingResponse updateShipping(Long orderId, Long adminId, UpdateShippingRequest request) {
        Shipping shipping = shippingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping not found for order: " + orderId));

        ShippingStatus from = shipping.getShippingStatus();
        ShippingStatus to = request.getShippingStatus();
        if (!ShippingStatusTransition.canTransition(from, to)) {
            throw new BadRequestException("Cannot change shipping status from " + from + " to " + to);
        }

        // Trạng thái đơn tương ứng với trạng thái giao hàng (null = không đổi đơn)
        OrderStatus targetOrderStatus = switch (to) {
            case PROCESSING -> OrderStatus.PROCESSING;
            case SHIPPING -> OrderStatus.SHIPPED;
            case DELIVERED -> OrderStatus.DELIVERED;
            default -> null;
        };
        
        
        
        // Hàng đã về kho: đơn phải đang SHIPPED mới đóng được (kiểm tra trước khi đổi dữ liệu)
        if (to == ShippingStatus.RETURNED
                && !OrderStatusTransition.canCancelOnReturn(shipping.getOrder().getStatus())) {
            throw new BadRequestException(
                    "Cannot return goods while order is " + shipping.getOrder().getStatus());
        }

        // Kiểm tra hết rồi mới thay đổi dữ liệu
        if (targetOrderStatus != null) {
            Order order = shipping.getOrder();
            PaymentMethod method = order.getLatestPayment().getMethod();
            if (!OrderStatusTransition.canTransition(method, order.getStatus(), targetOrderStatus)) {
                throw new BadRequestException(
                        "Cannot move shipping to " + to + " while order is " + order.getStatus());
            }

            User admin = userRepository.findById(adminId)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));

            orderStatusLogRepository.save(OrderStatusLog.builder()
                    .order(order)
                    .oldStatus(order.getStatus())
                    .newStatus(targetOrderStatus)
                    .changedBy(admin)
                    .build());
            order.setStatus(targetOrderStatus);
            orderRepository.save(order);
        }
        
        if (to == ShippingStatus.RETURNED) {
            closeOrderOnReturn(shipping.getOrder(), adminId);
        }

        shipping.setShippingStatus(to);
        if (request.getTrackingCode() != null && !request.getTrackingCode().isBlank()) {
            shipping.setTrackingCode(request.getTrackingCode().trim());
        }
        return shippingMapper.toShippingResponse(shippingRepository.save(shipping));
    }
    
    private void closeOrderOnReturn(Order order, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));

        for (OrderItem item : order.getOrderItems()) {
            productService.restoreStock(item.getProduct().getId(), item.getQuantity());
        }

        for (Payment payment : order.getPayments()) {
            if (PaymentStatusTransition.canTransition(payment.getStatus(), PaymentStatus.CANCELLED)) {
                payment.setStatus(PaymentStatus.CANCELLED);
            } else if (payment.getStatus() == PaymentStatus.SUCCESS) {
                refundService.createRefundForReturn(payment);
            }
        }

        orderStatusLogRepository.save(OrderStatusLog.builder()
                .order(order)
                .oldStatus(order.getStatus())
                .newStatus(OrderStatus.CANCELLED)
                .changedBy(admin)
                .build());
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}