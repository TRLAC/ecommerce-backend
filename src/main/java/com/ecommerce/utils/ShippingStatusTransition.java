package com.ecommerce.utils;

import java.util.Map;
import java.util.Set;

import com.ecommerce.enums.ShippingStatus;

public class ShippingStatusTransition {
	private ShippingStatusTransition() {};
	
	private static final Map<ShippingStatus, Set<ShippingStatus>> SHIPPING_TRANSITION = Map.of(
			ShippingStatus.PENDING, Set.of(ShippingStatus.PROCESSING),
			ShippingStatus.PROCESSING, Set.of(ShippingStatus.SHIPPING),
			ShippingStatus.SHIPPING, Set.of(ShippingStatus.DELIVERED, ShippingStatus.FAILED),
			ShippingStatus.FAILED, Set.of(ShippingStatus.RETURNED)
			);
			
	public static boolean canTransition(ShippingStatus from, ShippingStatus to) {
		return SHIPPING_TRANSITION.getOrDefault(from, Set.of()).contains(to);
	}

}
