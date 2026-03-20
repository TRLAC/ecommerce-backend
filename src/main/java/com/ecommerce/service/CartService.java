package com.ecommerce.service;
import java.util.Optional;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
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
import com.ecommerce.mapper.CartMapper;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Transactional
    public CartResponse addToCart(Long userId, CreateCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + userId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity() || product.getStatus() != ProductStatus.ACTIVE) {
        	throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm không đủ hoặc không còn kinh doanh");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), product.getProductId())
                .ifPresentOrElse(item -> {
                    int newQty = item.getQuantity() + request.getQuantity();
                    if (newQty > product.getStockQuantity())
                    	throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Số lượng tồn kho không đủ");
                    item.setQuantity(newQty);
                    cartItemRepository.save(item);
                }, () -> {
                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(request.getQuantity())
                            .priceSnapshot(product.getPrice())
                            .build();
                    cart.getItems().add(newItem);
                    cartItemRepository.save(newItem);
                });

        return cartMapper.toCartResponse(cartRepository.findByIdWithItems(cart.getId()).orElse(cart));
    }

    @Transactional
    public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new RuntimeException("Cart item không tồn tại"));

        if (item.getProduct().getStockQuantity() < request.getQuantity())
        	throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Số lượng tồn kho không đủ");

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        Cart cart = cartRepository.findByIdWithItems(item.getCart().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));

        return cartMapper.toCartResponse(cart);
    }

    @Transactional
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

    @Transactional
    public void clearCart(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        cartOpt.ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }
}