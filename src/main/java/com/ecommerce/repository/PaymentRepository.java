package com.ecommerce.repository;

import com.ecommerce.entity.Payment;
import com.ecommerce.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);
    
    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
}