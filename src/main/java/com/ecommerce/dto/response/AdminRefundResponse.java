package com.ecommerce.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.RefundStatus;

import lombok.Builder;
import lombok.Data;

/** Dòng hoàn tiền dành cho trang admin: có thêm thông tin đơn hàng và khách. */
@Data
@Builder
public class AdminRefundResponse {

    private Long refundId;
    private Long orderId;
    private Long paymentId;
    private PaymentMethod paymentMethod;
    private String userFullName;
    private String userEmail;
    private BigDecimal amount;
    private String reason;
    private RefundStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
