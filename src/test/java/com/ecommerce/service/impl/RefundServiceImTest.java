package com.ecommerce.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.dto.request.RefundRequest;
import com.ecommerce.dto.request.UpdateRefundStatusRequest;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Refund;
import com.ecommerce.entity.User;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.enums.RefundStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.mapper.RefundMapper;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderStatusLogRepository;
import com.ecommerce.repository.RefundRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.ProductService;

@ExtendWith(MockitoExtension.class)
class RefundServiceImTest {

    @Mock RefundRepository refundRepository;
    @Mock OrderRepository orderRepository;
    @Mock OrderStatusLogRepository orderStatusLogRepository;
    @Mock UserRepository userRepository;
    @Mock ProductServiceImpl productService;
    @Mock RefundMapper refundMapper;

    @InjectMocks RefundServiceImpl refundService;

    // Đơn đã thanh toán thành công, có 1 sản phẩm (id 1, số lượng 2)
    private Order buildPaidOrder(PaymentMethod method) {
        Product product = new Product();
        product.setId(1L);
        OrderItem item = OrderItem.builder().product(product).quantity(2).build();

        Order order = Order.builder()
                .id(5L)
                .status(OrderStatus.PAID)
                .orderItems(new ArrayList<>(List.of(item)))
                .build();
        order.addPayment(Payment.builder()
                .id(9L)
                .method(method)
                .status(PaymentStatus.SUCCESS)
                .amount(new BigDecimal("1000"))
                .build());
        return order;
    }

    private RefundRequest refundRequest() {
        RefundRequest request = new RefundRequest();
        request.setReason("Doi y");
        return request;
    }

    @Test
    void requestRefund_paidOnlineOrder_createsPendingRefundWithFullAmount() {
        Order order = buildPaidOrder(PaymentMethod.VNPAY);
        when(orderRepository.findByIdAndUser_Id(5L, 11L)).thenReturn(Optional.of(order));

        refundService.requestRefund(5L, 11L, refundRequest());

        ArgumentCaptor<Refund> captor = ArgumentCaptor.forClass(Refund.class);
        verify(refundRepository).save(captor.capture());
        assertEquals(RefundStatus.PENDING, captor.getValue().getStatus());
        assertEquals(new BigDecimal("1000"), captor.getValue().getAmount());
    }

    @Test
    void requestRefund_twice_isRejected() {
        Order order = buildPaidOrder(PaymentMethod.VNPAY);
        when(orderRepository.findByIdAndUser_Id(5L, 11L)).thenReturn(Optional.of(order));
        when(refundRepository.findByPaymentId(9L)).thenReturn(Optional.of(new Refund()));

        assertThrows(BadRequestException.class,
                () -> refundService.requestRefund(5L, 11L, refundRequest()));

        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void requestRefund_codOrder_isRejected() {
        Order order = buildPaidOrder(PaymentMethod.COD);
        when(orderRepository.findByIdAndUser_Id(5L, 11L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class,
                () -> refundService.requestRefund(5L, 11L, refundRequest()));
    }

    @Test
    void updateRefundStatus_completed_refundsPaymentAndOrderAndRestoresStock() {
        Order order = buildPaidOrder(PaymentMethod.VNPAY);
        Payment payment = order.getPayments().get(0);
        Refund refund = Refund.builder()
                .id(2L)
                .payment(payment)
                .status(RefundStatus.APPROVED)
                .amount(new BigDecimal("1000"))
                .build();
        User admin = new User();
        admin.setId(1L);
        when(refundRepository.findById(2L)).thenReturn(Optional.of(refund));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        UpdateRefundStatusRequest request = new UpdateRefundStatusRequest();
        request.setStatus(RefundStatus.COMPLETED);
        refundService.updateRefundStatus(2L, 1L, request);

        assertEquals(RefundStatus.COMPLETED, refund.getStatus());
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        assertEquals(OrderStatus.REFUNDED, order.getStatus());
        verify(productService).restoreStock(1L, 2);
    }

    @Test
    void updateRefundStatus_pendingToCompleted_isRejected() {
        Order order = buildPaidOrder(PaymentMethod.VNPAY);
        Refund refund = Refund.builder()
                .id(2L)
                .payment(order.getPayments().get(0))
                .status(RefundStatus.PENDING)
                .build();
        when(refundRepository.findById(2L)).thenReturn(Optional.of(refund));

        UpdateRefundStatusRequest request = new UpdateRefundStatusRequest();
        request.setStatus(RefundStatus.COMPLETED);

        assertThrows(BadRequestException.class,
                () -> refundService.updateRefundStatus(2L, 1L, request));
        assertEquals(OrderStatus.PAID, order.getStatus());
    }
}