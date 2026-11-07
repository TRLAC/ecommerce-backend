package com.ecommerce.mapper;

import org.springframework.stereotype.Component;

import com.ecommerce.dto.response.PaymentResponse;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.Refund;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentMapper {
	
	   private final RefundMapper refundMapper;

    public PaymentResponse toPaymentResponse(Payment payment) {
        if (payment == null) return null;

        return PaymentResponse.builder()
        		 .paymentId(payment.getId())
                 .orderId(payment.getOrder() != null
                         ? payment.getOrder().getId() : null)
                .method(payment.getMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransaction())
                .paidAt(payment.getPaidAt())
                .refund(refundMapper.toRefundResponse(
                        payment.getRefund()))
                .build();
    }

}
