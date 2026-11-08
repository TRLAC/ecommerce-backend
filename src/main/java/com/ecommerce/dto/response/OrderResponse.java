package com.ecommerce.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ecommerce.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
	  private Long orderId;
	    private Long userId;
	    private String userFullName;
	    private String userEmail;
	    private BigDecimal totalAmount;
	    private OrderStatus status;
	    private LocalDateTime createdAt;
	    private LocalDateTime updatedAt;
	    private List<OrderItemResponse> items;
	    private List<PaymentResponse> payments;
	    private ShippingResponse shipping;
	    private List<OrderStatusLogResponse> statusLogs;
}
