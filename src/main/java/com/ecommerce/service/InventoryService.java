package com.ecommerce.service;

public interface InventoryService {
    void deductStock(Long orderId);
    void restoreStock(Long orderId);
}