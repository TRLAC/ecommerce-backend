package com.ecommerce.repository;

import com.ecommerce.entity.Payment;
import com.ecommerce.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder_Id(Long orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByOrder_IdAndStatus(Long orderId, PaymentStatus status);
}