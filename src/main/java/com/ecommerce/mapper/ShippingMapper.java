package com.ecommerce.mapper;

import com.ecommerce.dto.response.ShippingResponse;
import com.ecommerce.entity.Shipping;

public class ShippingMapper {

	public ShippingResponse toShippingResponse(Shipping shipping) {
	    if (shipping == null) return null;

	    return ShippingResponse.builder()
	            .shippingId(shipping.getId())
	            .orderId(shipping.getOrder() != null ? shipping.getOrder().getId() : null)
	            .address(shipping.getAddress())
	            .shippingStatus(shipping.getShippingStatus())
	            .trackingCode(shipping.getTrackingCode())
	            .createdAt(shipping.getCreatedAt())
	            .updatedAt(shipping.getUpdatedAt())
	            .build();
	}

}
