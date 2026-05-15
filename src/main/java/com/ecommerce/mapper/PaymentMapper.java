package com.ecommerce.mapper;

import org.springframework.stereotype.Component;

import com.ecommerce.dto.response.PaymentResponse;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.Refund;

@Component
public class PaymentMapper {

    public PaymentResponse toPaymentResponse(Payment payment) {
        if (payment == null) return null;
        
        Refund refund = payment.getRefund();

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .method(payment.getMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paidAt(payment.getPaidAt())
                .build();
    }

}
