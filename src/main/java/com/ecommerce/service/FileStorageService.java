package com.ecommerce.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String AVATARS = "avatars";
    String PRODUCTS = "products";

    String save(MultipartFile file, String subDir);

    void delete(String fileName, String subDir);
}