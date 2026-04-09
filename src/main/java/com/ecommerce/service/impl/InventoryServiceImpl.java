package com.ecommerce.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.InventoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    public void deductStock(Long orderId) {

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getOrderItems() == null) return;

        for (OrderItem item : order.getOrderItems()) {

            int updated = productRepository.deductStock(
                    item.getProduct().getProductId(),
                    item.getQuantity()
            );

            if (updated == 0) {
                throw new RuntimeException("Insufficient stock for product: "
                        + item.getProduct().getName());
            }
        }
    }

    @Override
    public void restoreStock(Long orderId) {

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getOrderItems() == null) return;

        for (OrderItem item : order.getOrderItems()) {

            productRepository.restoreStock(
                    item.getProduct().getProductId(),
                    item.getQuantity()
            );
        }
    }
}