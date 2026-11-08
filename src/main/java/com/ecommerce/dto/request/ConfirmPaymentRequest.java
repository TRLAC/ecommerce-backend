package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConfirmPaymentRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    // Cổng giả lập báo kết quả: true = thành công, false = thất bại
    private boolean success = true;
}