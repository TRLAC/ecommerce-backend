package com.ecommerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
	@Email
    @NotBlank
    private String email;
	@NotBlank
	@Size(min = 2, max = 100)
	private String fullname;
	@NotBlank
	@Size(min = 8, max = 72)
    private String password;
}