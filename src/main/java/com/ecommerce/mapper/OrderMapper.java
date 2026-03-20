package com.ecommerce.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ecommerce.dto.response.OrderItemResponse;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.OrderStatusLogResponse;
import com.ecommerce.dto.response.PaymentResponse;
import com.ecommerce.dto.response.RefundResponse;
import com.ecommerce.dto.response.ShippingResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderStatusLog;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Refund;
import com.ecommerce.entity.Shipping;

@Component
public class OrderMapper {

    public OrderResponse toOrderResponse(Order order) {
        if (order == null) return null;

        return OrderResponse.builder()
                .Id(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userFullName(order.getUser() != null ? order.getUser().getFullName() : null)
                .userEmail(order.getUser() != null ? order.getUser().getEmail() : null)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(toOrderItemResponseList(order.getOrderItems()))
                .payment(toPaymentResponse(order.getPayment()))
                .shipping(toShippingResponse(order.getShipping()))
                .statusLogs(toStatusLogResponseList(order.getStatusLogs()))
                .build();
    }

    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        if (item == null) return null;
        
        Product product = item.getProduct();
        
     // Product.imageUrl is a plain String column (image_url) — NOT a collection
        String imageUrl = null;
        if (product != null
                && product.getImages() != null
                && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().get(0).getImageUrl();
        }

        return OrderItemResponse.builder()
                .orderItemId(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getProductId() : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .productImageUrl(imageUrl)
                .quantity(item.getQuantity())
                .priceSnapshot(item.getPriceSnapshot())
                .subtotal(item.getPriceSnapshot() != null && item.getQuantity() != null
                        ? item.getPriceSnapshot().multiply(java.math.BigDecimal.valueOf(item.getQuantity()))
                        : null)
                .build();
    }

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
                .refund(toRefundResponse(refund))
                .build();
    }

    public ShippingResponse toShippingResponse(Shipping shipping) {
        if (shipping == null) return null;

        return ShippingResponse.builder()
                .shippingId(shipping.getId())
                .address(shipping.getAddress())
                .shippingStatus(shipping.getShippingStatus())
                .trackingCode(shipping.getTrackingCode())
                .createdAt(shipping.getCreatedAt())
                .updatedAt(shipping.getUpdatedAt())
                .build();
    }

    public RefundResponse toRefundResponse(Refund refund) {
        if (refund == null) return null;

        return RefundResponse.builder()
                .refundId(refund.getId())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .createdAt(refund.getCreatedAt())
                .updatedAt(refund.getUpdatedAt())
                .build();
    }

    public OrderStatusLogResponse toStatusLogResponse(OrderStatusLog log) {
        if (log == null) return null;

        return OrderStatusLogResponse.builder()
                .logId(log.getId())
                .oldStatus(log.getOldStatus())
                .newStatus(log.getNewStatus())
                .changedByName(log.getChangedBy() != null ? log.getChangedBy().getFullName() : "SYSTEM")
                .changedAt(log.getChangedAt())
                .build();
    }

    public List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items) {
        if (items == null) return Collections.emptyList();
        return items.stream().map(this::toOrderItemResponse).collect(Collectors.toList());
    }

    public List<OrderStatusLogResponse> toStatusLogResponseList(List<OrderStatusLog> logs) {
        if (logs == null) return Collections.emptyList();
        return logs.stream().map(this::toStatusLogResponse).collect(Collectors.toList());
    }
}