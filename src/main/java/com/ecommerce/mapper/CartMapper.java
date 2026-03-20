package com.ecommerce.mapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ecommerce.dto.response.CartItemResponse;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;

@Component
public class CartMapper {

    public CartItemResponse toCartItemResponse(CartItem item) {
        if (item == null) return null;

        BigDecimal price = item.getPriceSnapshot() != null ? item.getPriceSnapshot()
                                                           : item.getProduct().getPrice();
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(item.getQuantity()));

        String imageUrl = (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty())
                          ? item.getProduct().getImages().get(0).getImageUrl()
                          : null;

        return CartItemResponse.builder()
                .cartItemId(item.getId())
                .productId(item.getProduct().getProductId())
                .productName(item.getProduct().getName())
                .productImageUrl(imageUrl)
                .productPrice(price)
                .productBrand(item.getProduct().getBrand() != null ? item.getProduct().getBrand().getName() : null)
                .quantity(item.getQuantity())
                .totalPrice(totalPrice)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public CartResponse toCartResponse(Cart cart) {
        if (cart == null) return null;

        List<CartItemResponse> items = cart.getItems() != null 
                ? cart.getItems().stream().map(this::toCartItemResponse).toList()
                : Collections.emptyList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .cartItems(items)
                .totalItems(items.size())
                .totalAmount(totalAmount)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}