package com.ecommerce.utils;

import java.util.Map;
import java.util.Set;

import com.ecommerce.enums.PaymentStatus;

public class PaymentStatusTransition {
	private PaymentStatusTransition() {};
	
	private static final Map<PaymentStatus, Set<PaymentStatus>> PAYMENT_TRANSITION = Map.of(
			PaymentStatus.PENDING, Set.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED),
			PaymentStatus.SUCCESS, Set.of(PaymentStatus.REFUNDED)
			);
	
	public static boolean canTransition(PaymentStatus from, PaymentStatus to) {
		return PAYMENT_TRANSITION.getOrDefault(from, Set.of()).contains(to);
	}
			

}
