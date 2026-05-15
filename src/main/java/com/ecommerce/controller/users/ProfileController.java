package com.ecommerce.controller.users;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
@RequestMapping("/api/profile")
public class ProfileController {
	private final UserService userService;
	
	public ProfileController(UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping
	public ResponseEntity<ProfileResponse> getProfile() {
	    return ResponseEntity.ok(userService.getProfile());
	}
	
	@PutMapping
	public ResponseEntity<?> updateProfile(
	        @Valid @RequestBody UpdateProfileRequest request
	) {
		  userService.updateProfile(request);
	    return ResponseEntity.ok(
	            Map.of("message", "Profile updated successfully")
	    );
	}
}
