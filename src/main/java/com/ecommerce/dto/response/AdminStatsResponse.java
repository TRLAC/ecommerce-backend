package com.ecommerce.dto.response;

import java.math.BigDecimal;
import java.util.Map;

import com.ecommerce.enums.OrderStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsResponse {

    private long totalUsers;
    private long totalOrders;
    private long totalProducts;
    /** Số hoàn tiền đang chờ duyệt (RefundStatus.PENDING). */
    private long pendingRefunds;
    /** Sản phẩm ACTIVE có tồn kho <= ngưỡng (xem AdminStatsServiceImpl.LOW_STOCK_THRESHOLD). */
    private long lowStockProducts;
    /** Tổng tiền thực thu = tổng amount của payment SUCCESS. */
    private BigDecimal revenue;
    /** Số đơn theo từng trạng thái (đủ mọi OrderStatus, thiếu thì = 0). */
    private Map<OrderStatus, Long> ordersByStatus;
}
