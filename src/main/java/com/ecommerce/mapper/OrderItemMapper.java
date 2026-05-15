package com.ecommerce.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.ecommerce.dto.response.OrderItemResponse;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;

public class OrderItemMapper {

	  public OrderItemResponse toOrderItemResponse(OrderItem item) {
	        if (item == null) return null;
	        
	        Product product = item.getProduct();
	        
	     // Product.imageUrl is a plain String column (image_url) — NOT a collection
	        String imageUrl = null;
	        if (product != null
	                && product.getImages() != null
	                && !product.getImages().isEmpty()) {
	            imageUrl = product.getImages().get(0).getImageUrl();
	        }

	        return OrderItemResponse.builder()
	                .orderItemId(item.getId())
	                .productId(item.getProduct() != null ? item.getProduct().getProductId() : null)
	                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
	                .productImageUrl(imageUrl)
	                .quantity(item.getQuantity())
	                .priceSnapshot(item.getPriceSnapshot())
	                .subtotal(item.getPriceSnapshot() != null && item.getQuantity() != null
	                        ? item.getPriceSnapshot().multiply(java.math.BigDecimal.valueOf(item.getQuantity()))
	                        : null)
	                .build();
	    }

	    public List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items) {
	        if (items == null) return Collections.emptyList();
	        return items.stream().map(this::toOrderItemResponse).collect(Collectors.toList());
	    }

}
