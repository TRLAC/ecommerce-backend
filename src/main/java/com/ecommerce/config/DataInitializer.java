package com.ecommerce.config;

import org.springframework.stereotype.Component;

import com.ecommerce.entity.Role;
import com.ecommerce.repository.RoleRepository;

import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {
	private final RoleRepository roleRepository;
	
	public DataInitializer(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}
	
	@PostConstruct
	public void initRole() { 
		if(roleRepository.findByName("ROLE_USER").isEmpty()) {
			Role userRole = new Role();
			userRole.setName("ROLE_USER");
			roleRepository.save(userRole);
		}
		
		if(roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
			Role adminRole = new Role();
			adminRole.setName("ROLE_ADMIN");
			roleRepository.save(adminRole);
		}
	}
}
