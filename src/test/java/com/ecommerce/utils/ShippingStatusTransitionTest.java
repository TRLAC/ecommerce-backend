package com.ecommerce.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ecommerce.enums.ShippingStatus;

class ShippingStatusTransitionTest {

    @Test
    void happyPath() {
        assertTrue(ShippingStatusTransition.canTransition(ShippingStatus.PENDING, ShippingStatus.PROCESSING));
        assertTrue(ShippingStatusTransition.canTransition(ShippingStatus.PROCESSING, ShippingStatus.SHIPPING));
        assertTrue(ShippingStatusTransition.canTransition(ShippingStatus.SHIPPING, ShippingStatus.DELIVERED));
    }

    @Test
    void failedDelivery_canBeReturned() {
        assertTrue(ShippingStatusTransition.canTransition(ShippingStatus.SHIPPING, ShippingStatus.FAILED));
        assertTrue(ShippingStatusTransition.canTransition(ShippingStatus.FAILED, ShippingStatus.RETURNED));
    }

    @Test
    void cannotSkipOrGoBack() {
        assertFalse(ShippingStatusTransition.canTransition(ShippingStatus.PENDING, ShippingStatus.DELIVERED));
        assertFalse(ShippingStatusTransition.canTransition(ShippingStatus.DELIVERED, ShippingStatus.RETURNED));
        assertFalse(ShippingStatusTransition.canTransition(ShippingStatus.RETURNED, ShippingStatus.PENDING));
    }
}