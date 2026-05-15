package com.ecommerce.service;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.request.CreateCartRequest;
import com.ecommerce.dto.request.UpdateCartItemRequest;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.enums.ProductStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.mapper.CartMapper;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    public CartResponse addToCart(Long userId, CreateCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + userId));

        Product product = productService.findById(request.getProductId());
        
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Product not active");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        Optional<CartItem> existingItem =
                cartItemRepository.findByCart_IdAndProduct_ProductId(
                        cart.getId(),
                        product.getProductId()
                );

        existingItem.ifPresentOrElse(item -> {

            int newQty = item.getQuantity() + request.getQuantity();

            productService.validateStock(product.getProductId(), newQty);

            item.setQuantity(newQty);
            cartItemRepository.save(item);

        }, () -> {

            productService.validateStock(product.getProductId(), request.getQuantity());

            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .priceSnapshot(product.getPrice())
                    .build();

            cartItemRepository.save(newItem);
        });

        return getCart(userId);
    }


    public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new RuntimeException("Cart item không tồn tại"));

        Product product = item.getProduct();
        productService.validateStock(product.getProductId(), request.getQuantity());

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        Cart cart = cartRepository.findByIdWithItems(item.getCart().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));

        return cartMapper.toCartResponse(cart);
    }

    public void removeCartItem(Long userId, Long cartItemId) {
        if (!cartItemRepository.existsByIdAndUserId(cartItemId, userId))
            throw new RuntimeException("Cart item không tồn tại");
        cartItemRepository.deleteByIdAndUserId(cartItemId, userId);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .user(userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy user")))
                        .build()));
        return cartMapper.toCartResponse(cart);
    }

    public void clearCart(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        cartOpt.ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }
}