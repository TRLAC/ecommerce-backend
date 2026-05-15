package com.ecommerce.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.dto.response.UserResponse;
import com.ecommerce.entity.RefreshToken;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.security.JwtUtil;
import com.ecommerce.service.RefreshTokenService;
import com.ecommerce.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserService userService,
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }
    
    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        User user = userService.register(request);

        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRoles()
        );

        RefreshToken refreshToken = refreshTokenService.create(user);
        addRefreshCookie(response, refreshToken.getToken());

        return ResponseEntity.ok(
            new AuthResponse(
                accessToken,
                toUserResponse(user)
            )
        );
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        

        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();

        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRoles()
        );

        RefreshToken refreshToken = refreshTokenService.create(user);

        addRefreshCookie(response, refreshToken.getToken());

        return ResponseEntity.ok(
        		 new AuthResponse(accessToken, toUserResponse(user))
        );
    }

    // ================= REFRESH =================
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String token = extractRefreshToken(request);

        RefreshToken oldToken = refreshTokenService.verify(token);

        // 🔥 rotate
        RefreshToken newToken = refreshTokenService.rotate(oldToken);
        
        User user = newToken.getUser();

        String newAccessToken = jwtUtil.generateAccessToken(
        		user.getEmail(),
        		user.getRoles()
        );

        addRefreshCookie(response, newToken.getToken());

        return ResponseEntity.ok(
        		new AuthResponse(newAccessToken, toUserResponse(user))
        );
    }

    // ================= LOGOUT =================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String token = extractRefreshTokenOrNull(request);

        if (token != null) {
            RefreshToken rt = refreshTokenService.verify(token);
            refreshTokenService.delete(rt);
        }

        clearRefreshCookie(response);

        return ResponseEntity.ok().build();
    }
    
    
    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .toList()
        );
    }

    // ================= HELPER =================

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No cookies");
        }

        return Arrays.stream(cookies)
                .filter(c -> "refreshToken".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Refresh token not found"
                ));
    }

    private String extractRefreshTokenOrNull(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(c -> "refreshToken".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    private void addRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // dev
        cookie.setPath("/api");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/api");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}