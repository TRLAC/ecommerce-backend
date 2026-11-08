package com.ecommerce.service;

import com.ecommerce.dto.request.CreateCartRequest;
import com.ecommerce.dto.request.UpdateCartItemRequest;
import com.ecommerce.dto.response.CartResponse;

public interface CartService {
    CartResponse addToCart(Long userId, CreateCartRequest request);
    CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request);
    void removeCartItem(Long userId, Long cartItemId);
    CartResponse getCart(Long userId);
    void clearCart(Long userId);
}