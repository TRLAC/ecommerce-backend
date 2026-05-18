package com.ecommerce.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ecommerce.dto.response.ProfileResponse;
import com.ecommerce.entity.User;

@Component
public class ProfileMapper {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public ProfileResponse mapToProfile(User user) {
        if (user == null) return null;

        // ✅ ghép full URL thay vì trả tên file thô
        String avatarUrl = user.getAvatar() != null
                ? baseUrl + "/uploads/avatars/" + user.getAvatar()
                : null;

        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                avatarUrl,
                user.getRoles()
                        .stream()
                        .map(role -> role.getName())
                        .toList()
        );
    }
}