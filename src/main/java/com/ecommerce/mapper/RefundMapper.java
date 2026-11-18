package com.ecommerce.mapper;

import org.springframework.stereotype.Component;

import com.ecommerce.dto.response.AdminRefundResponse;
import com.ecommerce.dto.response.RefundResponse;
import com.ecommerce.entity.Refund;

@Component
public class RefundMapper {

	  public RefundResponse toRefundResponse(Refund refund) {
	        if (refund == null) return null;

	        return RefundResponse.builder()
	                .refund(refund.getId())
	                .amount(refund.getAmount())
	                .reason(refund.getReason())
	                .status(refund.getStatus())
	                .createdAt(refund.getCreatedAt())
	                .updatedAt(refund.getUpdatedAt())
	                .build();
	    }

	  public AdminRefundResponse toAdminResponse(Refund refund) {
	        if (refund == null) return null;

	        var payment = refund.getPayment();
	        var order = payment != null ? payment.getOrder() : null;
	        var user = order != null ? order.getUser() : null;

	        return AdminRefundResponse.builder()
	                .refundId(refund.getId())
	                .orderId(order != null ? order.getId() : null)
	                .paymentId(payment != null ? payment.getId() : null)
	                .paymentMethod(payment != null ? payment.getMethod() : null)
	                .userFullName(user != null ? user.getFullName() : null)
	                .userEmail(user != null ? user.getEmail() : null)
	                .amount(refund.getAmount())
	                .reason(refund.getReason())
	                .status(refund.getStatus())
	                .createdAt(refund.getCreatedAt())
	                .updatedAt(refund.getUpdatedAt())
	                .build();
	    }

}
