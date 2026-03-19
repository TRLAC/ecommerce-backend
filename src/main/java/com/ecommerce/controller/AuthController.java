package com.ecommerce.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RefreshTokenRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.entity.RefreshToken;
import com.ecommerce.entity.User;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.security.JwtUtil;
import com.ecommerce.service.RefreshTokenService;
import com.ecommerce.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthenticationManager authenticationManager;
	private final UserService userService;
	private final JwtUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;
	
	public AuthController(AuthenticationManager authenticationManager,
			UserService userService,
			JwtUtil jwtUtil,
			RefreshTokenService refreshTokenService
	) {
		this.authenticationManager = authenticationManager;
		this.userService = userService;
		this.jwtUtil = jwtUtil;
		this.refreshTokenService = refreshTokenService;
	}
	
	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
		
		  userService.register(request);

		 return ResponseEntity.ok(
		            Map.of("message", "Register successfully")
		        );
	}
	
	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {		
		Authentication authentication = authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(
						request.getEmail(),
						request.getPassword())
		);		
		CustomUserDetails userDetails =(CustomUserDetails) authentication.getPrincipal();
		System.out.println(userDetails.getAuthorities());
		User user = userDetails.getUser();
		String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles());
		RefreshToken refreshToken = refreshTokenService.create(user);	
			return new AuthResponse(accessToken, refreshToken.getToken());
	}
	
	@PostMapping("/refresh")
	public AuthResponse refresh(@RequestBody @Valid RefreshTokenRequest request) {

	    // Verify refresh token
	    RefreshToken oldToken =
	            refreshTokenService.verify(request.refreshToken());

	    User user = oldToken.getUser();

	    // Rotate refresh token
	    RefreshToken newToken =
	            refreshTokenService.rotate(oldToken);

	    // Generate access token mới
	    String newAccessToken =
	            jwtUtil.generateAccessToken(user.getEmail(), user.getRoles());

	    // Response
	    return new AuthResponse(
	            newAccessToken,
	            newToken.getToken()
	    );
	}
	
	@PostMapping("/logout")
	public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest request) {
		RefreshToken token = refreshTokenService.verify(request.refreshToken());
	    refreshTokenService.delete(token);
	    return ResponseEntity.ok().build();
	}
}
