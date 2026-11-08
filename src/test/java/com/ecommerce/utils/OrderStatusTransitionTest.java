package com.ecommerce.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMethod;

class OrderStatusTransitionTest {

    @Test
    void online_pending_canBePaidOrCancelled() {
        assertTrue(OrderStatusTransition.canTransition(PaymentMethod.VNPAY, OrderStatus.PENDING, OrderStatus.PAID));
        assertTrue(OrderStatusTransition.canTransition(PaymentMethod.VNPAY, OrderStatus.PENDING, OrderStatus.CANCELLED));
        assertFalse(OrderStatusTransition.canTransition(PaymentMethod.VNPAY, OrderStatus.PENDING, OrderStatus.SHIPPED));
    }

    @Test
    void online_paid_canBeProcessedOrRefunded_butNotCancelled() {
        assertTrue(OrderStatusTransition.canTransition(PaymentMethod.MOMO, OrderStatus.PAID, OrderStatus.PROCESSING));
        assertTrue(OrderStatusTransition.canTransition(PaymentMethod.MOMO, OrderStatus.PAID, OrderStatus.REFUNDED));
        assertFalse(OrderStatusTransition.canTransition(PaymentMethod.MOMO, OrderStatus.PAID, OrderStatus.CANCELLED));
    }

    @Test
    void online_shippingChain() {
        assertTrue(OrderStatusTransition.canTransition(PaymentMethod.BANK_TRANSFER, OrderStatus.PROCESSING, OrderStatus.SHIPPED));
        assertTrue(OrderStatusTransition.canTransition(PaymentMethod.BANK_TRANSFER, OrderStatus.SHIPPED, OrderStatus.DELIVERED));
        assertFalse(OrderStatusTransition.canTransition(PaymentMethod.BANK_TRANSFER, OrderStatus.DELIVERED, OrderStatus.REFUNDED));
    }

    @Test
    void cod_flow() {
        assertTrue(OrderStatusTransition.canTransition(PaymentMethod.COD, OrderStatus.PENDING, OrderStatus.CONFIRMED));
        assertTrue(OrderStatusTransition.canTransition(PaymentMethod.COD, OrderStatus.PENDING, OrderStatus.CANCELLED));
        assertTrue(OrderStatusTransition.canTransition(PaymentMethod.COD, OrderStatus.CONFIRMED, OrderStatus.PROCESSING));
        assertTrue(OrderStatusTransition.canTransition(PaymentMethod.COD, OrderStatus.DELIVERED, OrderStatus.PAID));
    }

    @Test
    void cod_cannotSkipSteps() {
        assertFalse(OrderStatusTransition.canTransition(PaymentMethod.COD, OrderStatus.PENDING, OrderStatus.PAID));
        assertFalse(OrderStatusTransition.canTransition(PaymentMethod.COD, OrderStatus.PENDING, OrderStatus.PROCESSING));
        assertFalse(OrderStatusTransition.canTransition(PaymentMethod.COD, OrderStatus.PAID, OrderStatus.REFUNDED));
    }

    @Test
    void finalStatuses_haveNoTransitions() {
        assertFalse(OrderStatusTransition.canTransition(PaymentMethod.VNPAY, OrderStatus.CANCELLED, OrderStatus.PENDING));
        assertFalse(OrderStatusTransition.canTransition(PaymentMethod.VNPAY, OrderStatus.REFUNDED, OrderStatus.PAID));
        assertFalse(OrderStatusTransition.canTransition(PaymentMethod.COD, OrderStatus.CANCELLED, OrderStatus.CONFIRMED));
    }
}