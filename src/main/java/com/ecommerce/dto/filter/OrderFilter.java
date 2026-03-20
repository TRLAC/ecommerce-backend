package com.ecommerce.dto.filter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.ecommerce.enums.OrderStatus;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class OrderFilter {

    private Long userId;
 
    private OrderStatus status;
 
    private BigDecimal minAmount;
 
    private BigDecimal maxAmount;
 
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;
 
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;
 
    private int page = 0;
    private int size = 10;
    private String sortBy = "createAt";
    private String sortDir = "desc";
}
