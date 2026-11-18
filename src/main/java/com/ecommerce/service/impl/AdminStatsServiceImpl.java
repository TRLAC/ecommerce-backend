package com.ecommerce.service.impl;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.response.AdminStatsResponse;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.enums.ProductStatus;
import com.ecommerce.enums.RefundStatus;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.RefundRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.AdminStatsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminStatsServiceImpl implements AdminStatsService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        Map<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
        for (OrderStatus s : OrderStatus.values()) {
            byStatus.put(s, 0L);
        }
        List<Object[]> rows = orderRepository.countGroupedByStatus();
        for (Object[] row : rows) {
            byStatus.put((OrderStatus) row[0], (Long) row[1]);
        }

        BigDecimal revenue = paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS);

        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalOrders(orderRepository.count())
                .totalProducts(productRepository.count())
                .pendingRefunds(refundRepository.countByStatus(RefundStatus.PENDING))
                .lowStockProducts(productRepository
                        .countByStatusAndStockQuantityLessThanEqual(ProductStatus.ACTIVE, LOW_STOCK_THRESHOLD))
                .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                .ordersByStatus(byStatus)
                .build();
    }
}
