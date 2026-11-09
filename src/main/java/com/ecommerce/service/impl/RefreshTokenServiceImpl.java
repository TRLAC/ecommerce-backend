package com.ecommerce.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.config.JwtProperties;
import com.ecommerce.entity.RefreshToken;
import com.ecommerce.entity.User;
import com.ecommerce.repository.RefreshTokenRepository;
import com.ecommerce.service.RefreshTokenService;

@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
                                   JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    // Thời điểm hết hạn lấy từ jwt.refresh-expiration (mili giây)
    private LocalDateTime newExpiry() {
        return LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshExpiration()));
    }

    @Override
    public RefreshToken create(User user) {

        // chỉ giữ 1 token/user
        refreshTokenRepository.deleteByUser(user);

        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(newExpiry())
                .revoked(false)
                .build();

        return refreshTokenRepository.save(token);
    }

    @Override
    public RefreshToken verify(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token"
                ));

        if (refreshToken.isRevoked()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token revoked"
            );
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token expired"
            );
        }

        return refreshToken;
    }

    @Override
    public void delete(RefreshToken token) {
        refreshTokenRepository.delete(token);
    }

    @Override
    public RefreshToken rotate(RefreshToken oldToken) {
        // 1. revoke token cũ
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // 2. tạo token mới
        RefreshToken newToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(oldToken.getUser())
                .expiresAt(newExpiry())
                .build();

        return refreshTokenRepository.save(newToken);
    }
}