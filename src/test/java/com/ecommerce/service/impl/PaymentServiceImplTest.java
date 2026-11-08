package com.ecommerce.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.dto.request.ConfirmPaymentRequest;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatusLog;
import com.ecommerce.entity.Payment;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.mapper.PaymentMapper;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderStatusLogRepository;
import com.ecommerce.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock PaymentRepository paymentRepository;
    @Mock OrderRepository orderRepository;
    @Mock OrderStatusLogRepository orderStatusLogRepository;
    @Mock PaymentMapper paymentMapper;

    @InjectMocks PaymentServiceImpl paymentService;

    private Order order;
    private Payment payment;

    private void givenOrderWithPayment(OrderStatus orderStatus, PaymentStatus paymentStatus) {
        order = Order.builder()
                .id(5L)
                .status(orderStatus)
                .totalAmount(new BigDecimal("1000"))
                .build();
        payment = Payment.builder()
                .id(9L)
                .method(PaymentMethod.VNPAY)
                .status(paymentStatus)
                .transaction("TXN-1")
                .amount(new BigDecimal("1000"))
                .build();
        order.addPayment(payment);
    }

    private ConfirmPaymentRequest confirmRequest(boolean success) {
        ConfirmPaymentRequest request = new ConfirmPaymentRequest();
        request.setOrderId(5L);
        request.setTransactionId("TXN-1");
        request.setSuccess(success);
        return request;
    }

    @Test
    void confirmPayment_success_marksPaymentSuccessAndOrderPaid() {
        givenOrderWithPayment(OrderStatus.PENDING, PaymentStatus.PENDING);
        when(orderRepository.findByIdAndUser_Id(5L, 11L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByTransaction("TXN-1")).thenReturn(Optional.of(payment));

        paymentService.confirmPayment(11L, confirmRequest(true));

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertNotNull(payment.getPaidAt());
        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(orderStatusLogRepository).save(any(OrderStatusLog.class));
    }

    @Test
    void confirmPayment_failure_keepsOrderPending() {
        givenOrderWithPayment(OrderStatus.PENDING, PaymentStatus.PENDING);
        when(orderRepository.findByIdAndUser_Id(5L, 11L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByTransaction("TXN-1")).thenReturn(Optional.of(payment));

        paymentService.confirmPayment(11L, confirmRequest(false));

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(orderStatusLogRepository, never()).save(any(OrderStatusLog.class));
    }

    @Test
    void confirmPayment_twice_isRejected() {
        givenOrderWithPayment(OrderStatus.PAID, PaymentStatus.SUCCESS);
        when(orderRepository.findByIdAndUser_Id(5L, 11L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByTransaction("TXN-1")).thenReturn(Optional.of(payment));

        assertThrows(BadRequestException.class,
                () -> paymentService.confirmPayment(11L, confirmRequest(true)));

        verify(orderStatusLogRepository, never()).save(any(OrderStatusLog.class));
    }

    @Test
    void createPayment_forCod_isRejected() {
        assertThrows(BadRequestException.class,
                () -> paymentService.createPayment(5L, 11L, PaymentMethod.COD));
    }

    @Test
    void createPayment_forAlreadyPaidOrder_isRejected() {
        givenOrderWithPayment(OrderStatus.PAID, PaymentStatus.SUCCESS);
        when(orderRepository.findByIdAndUser_Id(5L, 11L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class,
                () -> paymentService.createPayment(5L, 11L, PaymentMethod.MOMO));
    }
}