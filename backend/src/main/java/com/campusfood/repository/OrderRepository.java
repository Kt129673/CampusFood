package com.campusfood.repository;

import com.campusfood.entity.Order;
import com.campusfood.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.delivery d " +
           "LEFT JOIN FETCH d.deliveryPartner " +
           "WHERE o.user.id = :userId " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product " +
           "LEFT JOIN FETCH o.user " +
           "WHERE o.status = :status")
    List<Order> findByStatus(@Param("status") OrderStatus status);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Order> findByStatusIn(List<OrderStatus> statuses);

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product " +
           "LEFT JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.delivery d " +
           "LEFT JOIN FETCH d.deliveryPartner " +
           "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);
}
