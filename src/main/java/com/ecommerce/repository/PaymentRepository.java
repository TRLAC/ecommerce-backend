package com.ecommerce.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.Payment;
import com.ecommerce.enums.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	
		List<Payment> findByOrderId(Long orderId);
	
		Optional<Payment> findFirstByOrderIdOrderByIdDesc(Long orderId);
		    
	    Optional<Payment> findByTransaction(String transactionId);
	
	    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);
	    
	    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
	    
	    boolean existsByOrderIdAndStatusIn(Long orderId, List<PaymentStatus> statuses);

	    // Tổng tiền thực thu: các payment có status truyền vào (SUCCESS); null nếu chưa có dòng nào
	    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
	    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);
	}