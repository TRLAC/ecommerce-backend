package com.ecommerce.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ecommerce.enums.RefundStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefundResponse {

    private Long refund;
    private BigDecimal amount;
    private String reason;
    private RefundStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}