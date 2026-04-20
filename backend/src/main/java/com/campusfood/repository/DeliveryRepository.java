package com.campusfood.repository;

import com.campusfood.entity.Delivery;
import com.campusfood.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByOrderId(Long orderId);

    List<Delivery> findByDeliveryPartnerId(Long deliveryPartnerId);

    List<Delivery> findByDeliveryPartnerIdAndStatus(Long deliveryPartnerId, DeliveryStatus status);

    List<Delivery> findByStatus(DeliveryStatus status);
}
