package com.ecommerce.service.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ecommerce.dto.response.BrandResponse;
import com.ecommerce.entity.Brand;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.BrandRepository;
import com.ecommerce.service.BrandService;

import lombok.RequiredArgsConstructor;

//com/ecommerce/service/impl/BrandServiceImpl.java
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

 private final BrandRepository brandRepository;

 @Override
 public List<BrandResponse> getAll() {
     return brandRepository.findAll(Sort.by("brandName"))
             .stream()
             .map(b -> new BrandResponse(b.getBrandId(), b.getBrandName()))
             .toList();
 }

 @Override
 public BrandResponse getById(Long id) {
     Brand brand = brandRepository.findById(id)
             .orElseThrow(() -> new ResourceNotFoundException("Brand not found: " + id));
     return new BrandResponse(brand.getBrandId(), brand.getBrandName());
 }
}