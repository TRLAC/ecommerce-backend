package com.ecommerce.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentResponse {

    private Long paymentId;
    private Long orderId;
    private PaymentMethod method;
    private BigDecimal amount;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime paidAt;
    private RefundResponse refund;
}