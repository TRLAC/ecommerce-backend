package com.ecommerce.mapper;

import com.ecommerce.dto.response.RefundResponse;
import com.ecommerce.entity.Refund;

public class RefundMapper {

	  public RefundResponse toRefundResponse(Refund refund) {
	        if (refund == null) return null;

	        return RefundResponse.builder()
	                .refundId(refund.getId())
	                .amount(refund.getAmount())
	                .reason(refund.getReason())
	                .status(refund.getStatus())
	                .createdAt(refund.getCreatedAt())
	                .updatedAt(refund.getUpdatedAt())
	                .build();
	    }

}
