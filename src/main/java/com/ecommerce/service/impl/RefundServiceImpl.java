package com.ecommerce.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.entity.Payment;
import com.ecommerce.entity.Refund;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.enums.RefundStatus;
import com.ecommerce.repository.PaymentRepository;
import com.ecommerce.repository.RefundRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefundServiceImpl {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    /**
     * Tạo refund nếu payment đã thanh toán
     */
    public boolean processRefundIfNeeded(Payment payment, String reason) {
        if (payment == null || payment.getStatus() != PaymentStatus.PAID) {
            log.info("No refund needed for paymentId={}", payment != null ? payment.getId() : null);
            return false;
        }

        Refund refund = Refund.builder()
                .payment(payment)
                .amount(payment.getAmount())
                .reason(reason)
                .status(RefundStatus.PENDING)
                .build();

        refundRepository.save(refund);

        // update payment status
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefund(refund);
        paymentRepository.save(payment);

        log.info("Refund created for paymentId={} reason={}", payment.getId(), reason);
        return true;
    }

    /**
     * Lấy payment theo orderId và process refund
     */
    public boolean processRefundByOrderId(Long orderId, String reason) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElse(null);

        return processRefundIfNeeded(payment, reason);
    }
}