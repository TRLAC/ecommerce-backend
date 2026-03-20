package com.ecommerce.repository;

import com.ecommerce.entity.Refund;
import com.ecommerce.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByPayment_Id(Long paymentId);

    Optional<Refund> findByPayment_Order_Id(Long orderId);

    boolean existsByPayment_Order_IdAndStatus(Long orderId, RefundStatus status);
}