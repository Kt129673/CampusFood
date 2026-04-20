package com.campusfood.repository;

import com.campusfood.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByAvailableTrue();

    List<Product> findByCategory(String category);

    List<Product> findByCategoryAndAvailableTrue(String category);

    Page<Product> findByAvailableTrue(Pageable pageable);

    List<Product> findByNameContainingIgnoreCase(String name);
}
