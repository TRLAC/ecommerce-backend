package com.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.Payment;
import com.ecommerce.enums.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);
    
    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
    
    boolean existsByOrderIdAndStatusIn(Long orderId, List<PaymentStatus> statuses);
}