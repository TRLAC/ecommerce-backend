package com.ecommerce.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlResolver {

    private final String baseUrl;

    public ImageUrlResolver(@Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // Tên file mới upload -> URL đầy đủ; ảnh cũ (đã là đường dẫn / URL) giữ nguyên
    public String productImage(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        if (stored.startsWith("/") || stored.startsWith("http")) {
            return stored;
        }
        return baseUrl + "/uploads/products/" + stored;
    }
}