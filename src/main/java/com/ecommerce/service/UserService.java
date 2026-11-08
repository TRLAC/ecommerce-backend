package com.ecommerce.service;

import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.request.UpdateProfileRequest;
import com.ecommerce.dto.response.ProfileResponse;
import com.ecommerce.entity.User;

public interface UserService {
    User findByEmail(String email);
    User register(RegisterRequest request);
    ProfileResponse getProfile();
    ProfileResponse updateProfile(UpdateProfileRequest request);
}