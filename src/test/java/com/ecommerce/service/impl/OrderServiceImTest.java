package com.ecommerce.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderStatusLog;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderStatusLogRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.ProductService;

@ExtendWith(MockitoExtension.class)
class OrderServiceImTest {

    @Mock OrderStatusLogRepository orderStatusLogRepository;
    @Mock CartRepository cartRepository;
    @Mock ProductServiceImpl productService;
    @Mock UserRepository userRepository;
    @Mock OrderRepository orderRepository;
    @Mock OrderMapper orderMapper;

    @InjectMocks OrderServiceImpl orderService;

    // Đơn có 1 sản phẩm (id 1, số lượng 2) và 1 payment
    private Order buildOrder(PaymentMethod method, OrderStatus status, PaymentStatus paymentStatus) {
        User user = new User();
        user.setId(11L);
        Product product = new Product();
        product.setId(1L);
        OrderItem item = OrderItem.builder().product(product).quantity(2).build();

        Order order = Order.builder()
                .id(5L)
                .user(user)
                .status(status)
                .orderItems(new ArrayList<>(List.of(item)))
                .build();
        order.addPayment(Payment.builder().method(method).status(paymentStatus).build());
        return order;
    }

    @Test
    void cancelOrder_pendingOrder_restoresStockCancelsPaymentsAndWritesLog() {
        Order order = buildOrder(PaymentMethod.COD, OrderStatus.PENDING, PaymentStatus.PENDING);
        when(orderRepository.findByIdAndUser_Id(5L, 11L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(5L, 11L);

        verify(productService).restoreStock(1L, 2);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(PaymentStatus.CANCELLED, order.getPayments().get(0).getStatus());

        ArgumentCaptor<OrderStatusLog> captor = ArgumentCaptor.forClass(OrderStatusLog.class);
        verify(orderStatusLogRepository).save(captor.capture());
        assertEquals(OrderStatus.PENDING, captor.getValue().getOldStatus());
        assertEquals(OrderStatus.CANCELLED, captor.getValue().getNewStatus());
    }

    @Test
    void cancelOrder_paidOnlineOrder_isRejected() {
        Order order = buildOrder(PaymentMethod.VNPAY, OrderStatus.PAID, PaymentStatus.SUCCESS);
        when(orderRepository.findByIdAndUser_Id(5L, 11L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.cancelOrder(5L, 11L));

        verify(productService, never()).restoreStock(anyLong(), anyInt());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(PaymentStatus.SUCCESS, order.getPayments().get(0).getStatus());
    }

    @Test
    void cancelOrder_ofAnotherUser_isNotFound() {
        when(orderRepository.findByIdAndUser_Id(5L, 99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.cancelOrder(5L, 99L));
    }

    @Test
    void updateOrderStatus_adminCancel_restoresStockAndCancelsPayments() {
        Order order = buildOrder(PaymentMethod.VNPAY, OrderStatus.PENDING, PaymentStatus.PENDING);
        User admin = new User();
        admin.setId(1L);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        orderService.updateOrderStatus(5L, OrderStatus.CANCELLED, 1L);

        verify(productService).restoreStock(1L, 2);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(PaymentStatus.CANCELLED, order.getPayments().get(0).getStatus());
    }

    @Test
    void updateOrderStatus_codDeliveredToPaid_marksPaymentSuccess() {
        Order order = buildOrder(PaymentMethod.COD, OrderStatus.DELIVERED, PaymentStatus.PENDING);
        User admin = new User();
        admin.setId(1L);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        orderService.updateOrderStatus(5L, OrderStatus.PAID, 1L);

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(PaymentStatus.SUCCESS, order.getPayments().get(0).getStatus());
        assertNotNull(order.getPayments().get(0).getPaidAt());
    }

    @Test
    void updateOrderStatus_onlineOrderCannotBeMarkedPaidByAdmin() {
        Order order = buildOrder(PaymentMethod.VNPAY, OrderStatus.PENDING, PaymentStatus.PENDING);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class,
                () -> orderService.updateOrderStatus(5L, OrderStatus.PAID, 1L));

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }
}