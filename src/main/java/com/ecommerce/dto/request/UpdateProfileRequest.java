package com.ecommerce.dto.request;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

	 @Size(max = 100)
	    private String fullName;

	    @Size(max = 15)
	    private String phone;

	    private MultipartFile avatar;
}