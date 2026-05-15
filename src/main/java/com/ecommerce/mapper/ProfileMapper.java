package com.ecommerce.mapper;

import org.springframework.stereotype.Component;

import com.ecommerce.dto.response.ProfileResponse;
import com.ecommerce.entity.User;
@Component
public class ProfileMapper {

	public ProfileResponse mapToProfile(User user) {
		 if (user == null) return null;
	    return new ProfileResponse(
	        user.getId(),
	        user.getEmail(),
	        user.getFullName(),
	        user.getPhone(),
	        user.getAvatar()
	    );
	}

}
