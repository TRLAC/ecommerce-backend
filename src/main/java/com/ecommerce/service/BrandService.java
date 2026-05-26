package com.ecommerce.service;

import java.util.List;

import com.ecommerce.dto.response.BrandResponse;

public interface BrandService {
	List<BrandResponse> getAll();
	BrandResponse getById(Long id);
}
