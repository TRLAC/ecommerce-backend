package com.ecommerce.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.Shipping;
import com.ecommerce.enums.ShippingStatus;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ShippingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShippingServiceImpl {

    private final ShippingRepository shippingRepository;
    private final OrderRepository orderRepository;

    public void createShipping(Long orderId, String address) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        Shipping shipping = Shipping.builder()
                .order(order)
                .address(address)
                .shippingStatus(ShippingStatus.PENDING)
                .build();

        shippingRepository.save(shipping);
        log.info("Shipping created for orderId={}", orderId);
    }

  
    public void updateShippingStatus(Long orderId, ShippingStatus status) {
        Shipping shipping = shippingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Shipping not found for order: " + orderId));

        shipping.setShippingStatus(status);
        shippingRepository.save(shipping);

        log.info("Shipping status updated for orderId={} -> {}", orderId, status);
    }
}