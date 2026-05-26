package com.ecommerce.controller.users;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.response.BrandResponse;
import com.ecommerce.service.BrandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

 private final BrandService brandService;

 @GetMapping
 public ResponseEntity<List<BrandResponse>> getAll() {
     return ResponseEntity.ok(brandService.getAll());
 }

 @GetMapping("/{id}")
 public ResponseEntity<BrandResponse> getById(@PathVariable Long id) {
     return ResponseEntity.ok(brandService.getById(id));
 }
}
