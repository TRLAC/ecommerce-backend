package com.ecommerce.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.request.UpdateShippingRequest;
import com.ecommerce.dto.response.ShippingResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatusLog;
import com.ecommerce.entity.Shipping;
import com.ecommerce.entity.User;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.ShippingStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.ShippingMapper;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderStatusLogRepository;
import com.ecommerce.repository.ShippingRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.ShippingService;
import com.ecommerce.utils.OrderStatusTransition;
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

        shipping.setShippingStatus(to);
        if (request.getTrackingCode() != null && !request.getTrackingCode().isBlank()) {
            shipping.setTrackingCode(request.getTrackingCode().trim());
        }
        return shippingMapper.toShippingResponse(shippingRepository.save(shipping));
    }
}