package com.ecommerce.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ecommerce.enums.OrderStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
public class OrderStatusLogResponse {
	 private Long logId;
	 private OrderStatus oldStatus;
	 private OrderStatus newStatus;
	 private String changedByName;
	 private LocalDateTime changedAt;
}
