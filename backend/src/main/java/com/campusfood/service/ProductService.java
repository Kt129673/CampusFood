package com.campusfood.service;

import com.campusfood.dto.request.InventoryUpdateRequest;
import com.campusfood.dto.request.ProductRequest;
import com.campusfood.dto.response.ProductResponse;
import com.campusfood.entity.Inventory;
import com.campusfood.entity.Product;
import com.campusfood.exception.ResourceNotFoundException;
import com.campusfood.repository.InventoryRepository;
import com.campusfood.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public List<ProductResponse> getAllAvailableProducts() {
        return productRepository.findByAvailableTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<ProductResponse> getAllAvailableProducts(Pageable pageable) {
        return productRepository.findByAvailableTrue(pageable)
                .map(this::mapToResponse);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return mapToResponse(product);
    }

    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndAvailableTrue(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .available(request.getAvailable())
                .build();

        Product saved = productRepository.save(product);

        // Create inventory entry
        Inventory inventory = Inventory.builder()
                .product(saved)
                .quantity(request.getInitialStock() != null ? request.getInitialStock() : 0)
                .build();
        inventoryRepository.save(inventory);

        log.info("Product created: {} (Category: {})", saved.getName(), saved.getCategory());
        return mapToResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        product.setAvailable(request.getAvailable());

        Product updated = productRepository.save(product);
        log.info("Product updated: {}", updated.getName());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        // Soft delete: mark as unavailable
        product.setAvailable(false);
        productRepository.save(product);
        log.info("Product soft-deleted: {}", product.getName());
    }

    @Transactional
    public ProductResponse updateInventory(Long productId, InventoryUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> Inventory.builder().product(product).quantity(0).build());

        inventory.setQuantity(request.getQuantity());
        inventoryRepository.save(inventory);

        log.info("Inventory updated for '{}': quantity = {}", product.getName(), request.getQuantity());
        return mapToResponse(product);
    }

    private ProductResponse mapToResponse(Product product) {
        Integer stock = inventoryRepository.findByProductId(product.getId())
                .map(Inventory::getQuantity)
                .orElse(0);

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .available(product.getAvailable())
                .stock(stock)
                .createdAt(product.getCreatedAt())
                .build();
    }
}
