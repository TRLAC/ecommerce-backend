package com.ecommerce.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.ecommerce.dto.response.OrderStatusLogResponse;
import com.ecommerce.entity.OrderStatusLog;

public class OrderStatusLogMapper {

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
    
    public List<OrderStatusLogResponse> toStatusLogResponseList(List<OrderStatusLog> logs) {
        if (logs == null) return Collections.emptyList();
        return logs.stream().map(this::toStatusLogResponse).collect(Collectors.toList());
    }

}
