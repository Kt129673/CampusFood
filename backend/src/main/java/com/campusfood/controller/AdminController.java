package com.campusfood.controller;

import com.campusfood.dto.request.InventoryUpdateRequest;
import com.campusfood.dto.request.OrderStatusUpdateRequest;
import com.campusfood.dto.request.ProductRequest;
import com.campusfood.dto.response.ApiResponse;
import com.campusfood.dto.response.OrderResponse;
import com.campusfood.dto.response.ProductResponse;
import com.campusfood.dto.response.UserResponse;
import com.campusfood.service.OrderService;
import com.campusfood.service.ProductService;
import com.campusfood.service.S3Service;
import com.campusfood.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;
    private final S3Service s3Service;

    // ========================
    // Product Management
    // ========================

    /**
     * Create a product via JSON body (imageUrl passed directly).
     */
    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", product));
    }

    /**
     * Create a product with an image file upload.
     * Accepts multipart/form-data with product fields + image file.
     */
    @PostMapping(value = "/products/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> createProductWithImage(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("category") String category,
            @RequestParam(value = "available", defaultValue = "true") Boolean available,
            @RequestParam(value = "initialStock", defaultValue = "0") Integer initialStock,
            @RequestParam("image") MultipartFile image) {

        // Upload image to S3
        String imageUrl = s3Service.uploadImage(image, "products");

        // Build product request
        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setDescription(description);
        request.setPrice(price);
        request.setCategory(category);
        request.setAvailable(available);
        request.setInitialStock(initialStock);
        request.setImageUrl(imageUrl);

        ProductResponse product = productService.createProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created with image", product));
    }

    /**
     * Update a product with optional image file upload.
     * If a new image is provided, the old one is deleted from S3.
     */
    @PutMapping(value = "/products/{id}/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductWithImage(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("category") String category,
            @RequestParam(value = "available", defaultValue = "true") Boolean available,
            @RequestParam(value = "initialStock", defaultValue = "0") Integer initialStock,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        // Get existing product to check for old image
        ProductResponse existing = productService.getProductById(id);

        String imageUrl;
        if (image != null && !image.isEmpty()) {
            // Delete old image from S3 if it exists
            if (existing.getImageUrl() != null && !existing.getImageUrl().isBlank()) {
                s3Service.deleteImage(existing.getImageUrl());
            }
            // Upload new image
            imageUrl = s3Service.uploadImage(image, "products");
        } else {
            // Keep existing image
            imageUrl = existing.getImageUrl();
        }

        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setDescription(description);
        request.setPrice(price);
        request.setCategory(category);
        request.setAvailable(available);
        request.setInitialStock(initialStock);
        request.setImageUrl(imageUrl);

        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        // Delete image from S3 before soft-deleting product
        ProductResponse existing = productService.getProductById(id);
        if (existing.getImageUrl() != null && !existing.getImageUrl().isBlank()) {
            s3Service.deleteImage(existing.getImageUrl());
        }
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    // ========================
    // Inventory Management
    // ========================

    @PutMapping("/inventory/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateInventory(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryUpdateRequest request) {
        ProductResponse product = productService.updateInventory(productId, request);
        return ResponseEntity.ok(ApiResponse.success("Inventory updated successfully", product));
    }

    // ========================
    // Order Management
    // ========================

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrderResponse> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderResponse order = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", order));
    }

    // ========================
    // Delivery Partners
    // ========================

    @GetMapping("/delivery-partners")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getDeliveryPartners() {
        List<UserResponse> partners = userService.getDeliveryPartners();
        return ResponseEntity.ok(ApiResponse.success(partners));
    }
}
