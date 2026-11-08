package com.ecommerce.service;

import com.ecommerce.entity.RefreshToken;
import com.ecommerce.entity.User;

public interface RefreshTokenService {
    RefreshToken create(User user);
    RefreshToken verify(String token);
    void delete(RefreshToken token);
    RefreshToken rotate(RefreshToken oldToken);
}