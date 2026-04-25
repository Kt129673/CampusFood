package com.campusfood.repository;

import com.campusfood.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.available = true ORDER BY p.createdAt DESC")
    List<Product> findByAvailableTrue();

    List<Product> findByCategory(String category);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.available = true ORDER BY p.createdAt DESC")
    List<Product> findByCategoryAndAvailableTrue(@Param("category") String category);

    Page<Product> findByAvailableTrue(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY p.createdAt DESC")
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT p.id FROM Product p WHERE p.available = true")
    List<Long> findAllAvailableProductIds();
}
