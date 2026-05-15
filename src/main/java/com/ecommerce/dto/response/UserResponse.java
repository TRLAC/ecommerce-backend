package com.ecommerce.dto.response;

import java.util.List;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    List<String> roles
) {}