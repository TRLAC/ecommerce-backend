package com.ecommerce.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.request.UpdateProfileRequest;
import com.ecommerce.dto.response.ProfileResponse;
import com.ecommerce.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class ProfileController {
	private final UserService userService;
	
	public ProfileController(UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping("/profile")
	public ResponseEntity<ProfileResponse> getProfile(
	        Principal principal
	) {
	    return ResponseEntity.ok(
	            userService.getProfile(principal.getName())
	    );
	}
	
	@PutMapping("/profile")
	public ResponseEntity<?> updateProfile(
	        @Valid @RequestBody UpdateProfileRequest request,
	        Principal principal
	) {
	    userService.updateProfile(principal.getName(), request);

	    return ResponseEntity.ok(
	            Map.of("message", "Profile updated successfully")
	    );
	}
}
