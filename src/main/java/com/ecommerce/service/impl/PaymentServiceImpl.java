package com.ecommerce.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.request.ConfirmPaymentRequest;
import com.ecommerce.dto.response.PaymentResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.PaymentMapper;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
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
    private final OrderService orderService;
    private final PaymentMapper paymentMapper; 

    // ================================================================
    // CREATE PAYMENT
    // ================================================================
    @Override
    public PaymentResponse createPayment(Long orderId, PaymentMethod method) {

        Order order = getOrder(orderId);

        if (paymentRepository.existsByOrderIdAndStatusIn(
                orderId,
                List.of(PaymentStatus.PENDING, PaymentStatus.PAID)
        )) {
            throw new BadRequestException("Payment already exists for order");
        }

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(method)
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);

        return paymentMapper.toPaymentResponse(payment);
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

	// ================================================================
    // CONFIRM PAYMENT
    // ================================================================
    @Override
    public PaymentResponse confirmPayment(ConfirmPaymentRequest request) {

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", request.getOrderId()));

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Payment already confirmed");
        }
        
        Order order = payment.getOrder();
        

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in PENDING state");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId(request.getTransactionId());
        payment.setPaidAt(LocalDateTime.now());
        
        paymentRepository.save(payment);
        
        orderService.markAsPaid(order.getId());
        
        log.info("Payment confirmed for orderId={}", order.getId());

        return paymentMapper.toPaymentResponse(payment);
    }

    // ================================================================
    // REFUND (BONUS - nên có)
    // ================================================================
    @Override
    public PaymentResponse refundIfPaid(Long orderId, String reason) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for order", orderId));

        if (payment.getStatus() != PaymentStatus.PAID) {
            return paymentMapper.toPaymentResponse(payment);
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        
        orderService.markAsRefunded(orderId);


        log.info("Refund processed for orderId={} reason={}", orderId, reason);

        return paymentMapper.toPaymentResponse(payment);
    }
}