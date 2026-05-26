package com.ecommerce.mapper;

import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductImage;

public class ProductMapper {

    public static ProductResponse toResponse(Product product) {

        if (product == null) {
            return null;
        }

        ProductResponse res = new ProductResponse();

        res.setProductId(product.getProductId());
        res.setName(product.getName());
        res.setPrice(product.getPrice());
        res.setStockQuantity(product.getStockQuantity());
        res.setStatus(product.getStatus());

        if (product.getBrand() != null) {
            res.setBrandId(product.getBrand().getBrandId());
            res.setBrandName(product.getBrand().getBrandName());
        }

        if (product.getCategory() != null) {
            res.setCategoryId(product.getCategory().getCategoryId());
            res.setCategoryName(product.getCategory().getCategoryName());
        }

        res.setRam(product.getRam());
        res.setStorage(product.getStorage());
        res.setBattery(product.getBattery());
        res.setScreenSize(product.getScreenSize());
        if (product.getImages() != null) {
        	res.setImages(
                    product.getImages()
                           .stream()
                           .map(ProductImage::getImageUrl)
                           .toList()
            );
        }

        return res;
    }
}