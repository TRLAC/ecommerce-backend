package com.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileResponse {
	private Long id;
	private String email;
	private String fullName;
	private String phone;
	private String  avatar;
}
