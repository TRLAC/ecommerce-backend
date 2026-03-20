package com.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	
	// Tìm item theo cartItemId và userId (để verify ownership)
    @Query("SELECT ci FROM CartItem ci WHERE ci.id = :cartItemId AND ci.cart.user.id = :userId")
    Optional<CartItem> findByIdAndUserId(@Param("cartItemId") Long cartItemId,
                                         @Param("userId") Long userId);
 
    // Tìm item đã tồn tại trong cart theo productId (để cộng dồn số lượng)
    Optional<CartItem> findByCart_IdAndProduct_ProductId(Long cartId, Long productId);
 
    // Kiểm tra ownership trước khi xóa
    @Query("SELECT COUNT(ci) > 0 FROM CartItem ci WHERE ci.id = :cartItemId AND ci.cart.user.id = :userId")
    boolean existsByIdAndUserId(@Param("cartItemId") Long cartItemId,
                                @Param("userId") Long userId);
 
    // Xóa item theo cartItemId (sau khi đã verify ownership)
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.id = :cartItemId AND ci.cart.user.id = :userId")
    void deleteByIdAndUserId(@Param("cartItemId") Long cartItemId,
                             @Param("userId") Long userId);

}