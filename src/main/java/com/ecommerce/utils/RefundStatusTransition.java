package com.ecommerce.utils;

import java.util.Map;
import java.util.Set;

import com.ecommerce.enums.RefundStatus;

public class RefundStatusTransition {
	private RefundStatusTransition() {};
	
	private static final Map<RefundStatus ,Set<RefundStatus>> REFUND_TRANSITON = Map.of(
			RefundStatus.PENDING, Set.of(RefundStatus.APPROVED, RefundStatus.REJECTED),
			RefundStatus.APPROVED, Set.of(RefundStatus.COMPLETED)
			);

	public static boolean canTransition(RefundStatus from, RefundStatus to) {
		return REFUND_TRANSITON.getOrDefault(from, Set.of()).contains(to);
	}
}
