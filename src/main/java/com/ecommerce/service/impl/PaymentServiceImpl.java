package com.ecommerce.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.request.ConfirmPaymentRequest;
import com.ecommerce.dto.response.PaymentResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatusLog;
import com.ecommerce.entity.Payment;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.PaymentMapper;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderStatusLogRepository;
import com.ecommerce.repository.PaymentRepository;
import com.ecommerce.service.PaymentService;
import com.ecommerce.utils.OrderStatusTransition;
import com.ecommerce.utils.PaymentStatusTransition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentResponse createPayment(Long orderId, Long userId, PaymentMethod method) {
        if (method == PaymentMethod.COD) {
            throw new BadRequestException("COD is paid on delivery, no online payment needed");
        }

        Order order = findOwnedOrder(orderId, userId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Cannot pay for order with status: " + order.getStatus());
        }
        if (paymentRepository.existsByOrderIdAndStatus(orderId, PaymentStatus.SUCCESS)) {
            throw new BadRequestException("Order is already paid");
        }

        // Có lần thanh toán PENDING thì dùng lại; không thì tạo lần mới
        Payment payment = paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.PENDING)
                .stream().findFirst().orElse(null);

        if (payment == null) {
            payment = Payment.builder()
                    .method(method)
                    .amount(order.getTotalAmount())
                    .status(PaymentStatus.PENDING)
                    .build();
            order.addPayment(payment);
        } else {
            payment.setMethod(method);
        }

        // Mã giao dịch giả lập của cổng thanh toán
        payment.setTransaction("TXN-" + UUID.randomUUID());

        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse confirmPayment(Long userId, ConfirmPaymentRequest request) {
        Order order = findOwnedOrder(request.getOrderId(), userId);

        Payment payment = paymentRepository.findByTransaction(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for transaction: " + request.getTransactionId()));

        if (!payment.getOrder().getId().equals(order.getId())) {
            throw new BadRequestException("Transaction does not belong to this order");
        }

        PaymentStatus target = request.isSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        if (!PaymentStatusTransition.canTransition(payment.getStatus(), target)) {
            throw new BadRequestException(
                    "Cannot change payment status from " + payment.getStatus() + " to " + target);
        }

        if (target == PaymentStatus.SUCCESS) {
            if (!OrderStatusTransition.canTransition(payment.getMethod(), order.getStatus(), OrderStatus.PAID)) {
                throw new BadRequestException("Cannot mark order as PAID from status: " + order.getStatus());
            }

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());

            // Hệ thống tự đổi trạng thái nên changedBy để trống (hiện "SYSTEM")
            orderStatusLogRepository.save(OrderStatusLog.builder()
                    .order(order)
                    .oldStatus(order.getStatus())
                    .newStatus(OrderStatus.PAID)
                    .build());
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    private Order findOwnedOrder(Long orderId, Long userId) {
        return orderRepository.findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }
}