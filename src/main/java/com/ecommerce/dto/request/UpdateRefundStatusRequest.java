package com.ecommerce.dto.request;

import com.ecommerce.enums.RefundStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRefundStatusRequest {

    @NotNull(message = "Refund status is required")
    private RefundStatus status;
}