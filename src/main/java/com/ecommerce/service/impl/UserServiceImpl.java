package com.ecommerce.service.impl;

import java.util.Set;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.request.UpdateProfileRequest;
import com.ecommerce.dto.response.ProfileResponse;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.ProfileMapper;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.FileStorageService;
import com.ecommerce.service.UserService;
@Service
@Transactional
public class UserServiceImpl implements UserService{
	private final ProfileMapper profileMapper;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	 private final FileStorageService fileStorageService;
	
	public UserServiceImpl(ProfileMapper profileMapper ,UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,  FileStorageService fileStorageService) {
		this.profileMapper = profileMapper;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.fileStorageService = fileStorageService;
		
	}
	
	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found")); 
	}
	
	
	public User register(RegisterRequest request) {
		
		if(userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new BadRequestException("Email alrealy exists");
		}
		
		Role userRole = roleRepository.findByName("ROLE_USER")
				.orElseThrow(() -> new RuntimeException("ROLE not found"));
		
		User user = new User();
		user.setEmail(request.getEmail());
		user.setFullName(request.getFullname());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setRoles(Set.of(userRole));
		
		return userRepository.save(user);	
	}
	
	public ProfileResponse getProfile() {
		 String email = SecurityContextHolder.getContext()
		            .getAuthentication()
		            .getName();
	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    return profileMapper.mapToProfile(user);
	}
	
	public ProfileResponse updateProfile(UpdateProfileRequest request) {

	    String email = SecurityContextHolder.getContext()
	            .getAuthentication()
	            .getName();

	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    if (request.getFullName() != null) {
	        user.setFullName(request.getFullName());
	    }

	    if (request.getPhone() != null) {
	        user.setPhone(request.getPhone());
	    }
	    
	    if (request.getAddress() != null) {
	        user.setAddress(request.getAddress().trim());
	    }

	    if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
	        fileStorageService.delete(user.getAvatar(), FileStorageService.AVATARS);
	        String fileName = fileStorageService.save(request.getAvatar(), FileStorageService.AVATARS);
	        user.setAvatar(fileName);
	    }

	    User savedUser = userRepository.save(user);

	    return profileMapper.mapToProfile(savedUser);
	}
}
