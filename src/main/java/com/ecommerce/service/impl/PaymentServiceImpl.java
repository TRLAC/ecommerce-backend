package com.ecommerce.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.request.ConfirmPaymentRequest;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import com.ecommerce.service.InventoryService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final OrderService orderService;

    // ================================================================
    // CREATE PAYMENT
    // ================================================================
    @Override
    public void createPayment(Long orderId, PaymentMethod method) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(method)
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);
    }

    // ================================================================
    // CONFIRM PAYMENT
    // ================================================================
    @Override
    public void confirmPayment(ConfirmPaymentRequest request) {

        Payment payment = paymentRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", request.getOrderId()));

        Order order = payment.getOrder();

        // ❗ check trạng thái order
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in PENDING state");
        }

        // ❗ tránh double payment
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Payment already confirmed");
        }

        // ✅ update payment
        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId(request.getTransactionId());
        payment.setPaidAt(LocalDateTime.now());

        // ✅ update order
        orderService.markAsPaid(order.getId());

        // ✅ trừ kho
        inventoryService.deductStock(order.getId());
    }

    // ================================================================
    // REFUND (BONUS - nên có)
    // ================================================================
    @Override
    public void refundIfPaid(Long orderId, String reason) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for order", orderId));

        if (payment.getStatus() != PaymentStatus.PAID) {
            return; // không cần refund
        }

        payment.setStatus(PaymentStatus.REFUNDED);

        // 👉 hoàn kho
        inventoryService.restoreStock(orderId);

        log.info("Refund processed for orderId={} reason={}", orderId, reason);
    }
}