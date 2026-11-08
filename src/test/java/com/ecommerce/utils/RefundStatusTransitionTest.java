package com.ecommerce.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ecommerce.enums.RefundStatus;

class RefundStatusTransitionTest {

    @Test
    void pending_canBeApprovedOrRejected() {
        assertTrue(RefundStatusTransition.canTransition(RefundStatus.PENDING, RefundStatus.APPROVED));
        assertTrue(RefundStatusTransition.canTransition(RefundStatus.PENDING, RefundStatus.REJECTED));
    }

    @Test
    void approved_canBeCompleted() {
        assertTrue(RefundStatusTransition.canTransition(RefundStatus.APPROVED, RefundStatus.COMPLETED));
    }

    @Test
    void cannotSkipOrReopen() {
        assertFalse(RefundStatusTransition.canTransition(RefundStatus.PENDING, RefundStatus.COMPLETED));
        assertFalse(RefundStatusTransition.canTransition(RefundStatus.REJECTED, RefundStatus.APPROVED));
        assertFalse(RefundStatusTransition.canTransition(RefundStatus.COMPLETED, RefundStatus.PENDING));
    }
}