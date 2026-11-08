package com.ecommerce.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ecommerce.enums.PaymentStatus;

class PaymentStatusTransitionTest {

    @Test
    void pending_canSucceedFailOrBeCancelled() {
        assertTrue(PaymentStatusTransition.canTransition(PaymentStatus.PENDING, PaymentStatus.SUCCESS));
        assertTrue(PaymentStatusTransition.canTransition(PaymentStatus.PENDING, PaymentStatus.FAILED));
        assertTrue(PaymentStatusTransition.canTransition(PaymentStatus.PENDING, PaymentStatus.CANCELLED));
        assertFalse(PaymentStatusTransition.canTransition(PaymentStatus.PENDING, PaymentStatus.REFUNDED));
    }

    @Test
    void success_canOnlyBeRefunded() {
        assertTrue(PaymentStatusTransition.canTransition(PaymentStatus.SUCCESS, PaymentStatus.REFUNDED));
        assertFalse(PaymentStatusTransition.canTransition(PaymentStatus.SUCCESS, PaymentStatus.FAILED));
        assertFalse(PaymentStatusTransition.canTransition(PaymentStatus.SUCCESS, PaymentStatus.SUCCESS));
    }

    @Test
    void finalStatuses_cannotChange() {
        assertFalse(PaymentStatusTransition.canTransition(PaymentStatus.FAILED, PaymentStatus.SUCCESS));
        assertFalse(PaymentStatusTransition.canTransition(PaymentStatus.CANCELLED, PaymentStatus.SUCCESS));
        assertFalse(PaymentStatusTransition.canTransition(PaymentStatus.REFUNDED, PaymentStatus.PENDING));
    }
}