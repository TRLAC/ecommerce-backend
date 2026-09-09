package com.ecommerce.utils;

import java.util.Map;
import java.util.Set;

import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMethod;

public class OrderStatusTransition {
	private OrderStatusTransition() {}
	
	private static final Map<OrderStatus, Set<OrderStatus>> ONLINE_TRANSITION = Map.of(
			OrderStatus.PENDING, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED),
			OrderStatus.PAID, Set.of(OrderStatus.PROCESSING, OrderStatus.REFUNDED),
			OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED),
			OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED)
	);
	
	private static final Map<OrderStatus, Set<OrderStatus>> COD_TRANSITION = Map.of(
			OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
			OrderStatus.CONFIRMED, Set.of(OrderStatus.PROCESSING),
			OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED),
			OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
			OrderStatus.DELIVERED , Set.of(OrderStatus.PAID)
			);
			
			
	public static boolean canTransition(PaymentMethod method,OrderStatus from, OrderStatus to) {
		if(method == PaymentMethod.COD) {
			return COD_TRANSITION.getOrDefault(from, Set.of()).contains(to);
		} else {
			return ONLINE_TRANSITION.getOrDefault(from, Set.of()).contains(to);
		}
    }
	
}
