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
import com.campusfood.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    // ========================
    // Product Management
    // ========================

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", product));
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
