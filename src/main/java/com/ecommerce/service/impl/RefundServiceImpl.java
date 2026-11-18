package com.ecommerce.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.request.RefundRequest;
import com.ecommerce.dto.request.UpdateRefundStatusRequest;
import com.ecommerce.dto.response.AdminRefundResponse;
import com.ecommerce.dto.response.RefundResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderStatusLog;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.Refund;
import com.ecommerce.entity.User;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.enums.RefundStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.RefundMapper;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderStatusLogRepository;
import com.ecommerce.repository.RefundRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.RefundService;
import com.ecommerce.utils.OrderStatusTransition;
import com.ecommerce.utils.PaymentStatusTransition;
import com.ecommerce.utils.RefundStatusTransition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final UserRepository userRepository;
    private final ProductServiceImpl productService;
    private final RefundMapper refundMapper;

    @Override
    public RefundResponse requestRefund(Long orderId, Long userId, RefundRequest request) {
        Order order = orderRepository.findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Chỉ hoàn tiền cho lần thanh toán đã thành công
        Payment payment = order.getPayments().stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Order has no successful payment to refund"));

        // ONLINE: PAID -> REFUNDED mới hợp lệ (COD hoặc đơn đã giao thì bị chặn)
        if (!OrderStatusTransition.canTransition(payment.getMethod(), order.getStatus(), OrderStatus.REFUNDED)) {
            throw new BadRequestException("Order cannot be refunded in status: " + order.getStatus());
        }

        if (refundRepository.findByPaymentId(payment.getId()).isPresent()) {
            throw new BadRequestException("Refund already requested for this order");
        }

        Refund refund = Refund.builder()
                .payment(payment)
                .amount(payment.getAmount())
                .reason(request.getReason().trim())
                .status(RefundStatus.PENDING)
                .build();
        payment.setRefund(refund);

        return refundMapper.toRefundResponse(refundRepository.save(refund));
    }

    @Override
    public void createRefundForReturn(Payment payment) {
        if (refundRepository.findByPaymentId(payment.getId()).isPresent()) {
            return;
        }
        Refund refund = Refund.builder()
                .payment(payment)
                .amount(payment.getAmount())
                .reason("Delivery failed, goods returned to warehouse")
                .status(RefundStatus.PENDING)
                .build();
        payment.setRefund(refund);
        refundRepository.save(refund);
    }
    
    @Override
    public RefundResponse updateRefundStatus(Long refundId, Long adminId, UpdateRefundStatusRequest request) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found with id: " + refundId));

        RefundStatus to = request.getStatus();
        if (!RefundStatusTransition.canTransition(refund.getStatus(), to)) {
            throw new BadRequestException("Cannot change refund status from " + refund.getStatus() + " to " + to);
        }

        if (to == RefundStatus.COMPLETED) {
            Payment payment = refund.getPayment();
            Order order = payment.getOrder();

            // Kiểm tra hết rồi mới thay đổi dữ liệu
            if (!PaymentStatusTransition.canTransition(payment.getStatus(), PaymentStatus.REFUNDED)) {
                throw new BadRequestException("Cannot refund payment with status: " + payment.getStatus());
            }
            
            // Đơn đã CANCELLED + payment SUCCESS = hàng giao thất bại đã về kho (xem ShippingServiceImpl):
            // kho đã được cộng lại và đơn đã đóng, chỉ còn hoàn tiền cho khách.
            boolean returnedGoods = order.getStatus() == OrderStatus.CANCELLED;

            if (!returnedGoods
                    && !OrderStatusTransition.canTransition(payment.getMethod(), order.getStatus(), OrderStatus.REFUNDED)) {
                throw new BadRequestException("Cannot refund order with status: " + order.getStatus());
            }

            if (returnedGoods) {
                payment.setStatus(PaymentStatus.REFUNDED);
            } else {
                User admin = userRepository.findById(adminId)
                        .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));

                // Hàng chưa rời kho (đơn đang PAID) nên hoàn kho
                for (OrderItem item : order.getOrderItems()) {
                    productService.restoreStock(item.getProduct().getId(), item.getQuantity());
                }

                payment.setStatus(PaymentStatus.REFUNDED);

                orderStatusLogRepository.save(OrderStatusLog.builder()
                        .order(order)
                        .oldStatus(order.getStatus())
                        .newStatus(OrderStatus.REFUNDED)
                        .changedBy(admin)
                        .build());
                order.setStatus(OrderStatus.REFUNDED);
                orderRepository.save(order);
            }
        }

        refund.setStatus(to);
        return refundMapper.toRefundResponse(refundRepository.save(refund));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminRefundResponse> getRefunds(RefundStatus status, Pageable pageable) {
        Page<Refund> page = (status == null)
                ? refundRepository.findAll(pageable)
                : refundRepository.findByStatus(status, pageable);
        return page.map(refundMapper::toAdminResponse);
    }
}
