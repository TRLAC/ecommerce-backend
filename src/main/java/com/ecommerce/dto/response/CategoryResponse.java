package com.ecommerce.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponse {

    private Long categoryId;
    private String name;
    private String status;
    private Long parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
}
