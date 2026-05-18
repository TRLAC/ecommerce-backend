package com.ecommerce.dto.response;

import java.util.List;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    String phone,
    String avatar,
    List<String> roles
) {}