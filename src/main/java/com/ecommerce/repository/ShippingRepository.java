package com.ecommerce.repository;

import com.ecommerce.entity.Shipping;
import com.ecommerce.enums.ShippingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Long> {

    Optional<Shipping> findByOrder_Id(Long orderId);

    Optional<Shipping> findByTrackingCode(String trackingCode);

    boolean existsByOrder_IdAndShippingStatus(Long orderId, ShippingStatus shippingStatus);
}