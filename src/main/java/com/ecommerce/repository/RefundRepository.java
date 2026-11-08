package com.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.Refund;
import com.ecommerce.enums.RefundStatus;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByPaymentId(Long paymentId);

    Optional<Refund> findByPaymentOrderId(Long orderId);

    boolean existsByPaymentOrderIdAndStatus(Long orderId, RefundStatus status);
}