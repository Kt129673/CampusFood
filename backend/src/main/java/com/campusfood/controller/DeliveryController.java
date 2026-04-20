package com.campusfood.controller;

import com.campusfood.dto.request.DeliveryStatusUpdateRequest;
import com.campusfood.dto.response.ApiResponse;
import com.campusfood.dto.response.DeliveryResponse;
import com.campusfood.dto.response.OrderResponse;
import com.campusfood.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/available-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAvailableOrders() {
        List<OrderResponse> orders = deliveryService.getAvailableOrdersForDelivery();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PostMapping("/accept/{orderId}")
    public ResponseEntity<ApiResponse<DeliveryResponse>> acceptOrder(
            @PathVariable Long orderId,
            @RequestParam Long deliveryPartnerId) {
        DeliveryResponse delivery = deliveryService.acceptOrder(orderId, deliveryPartnerId);
        return ResponseEntity.ok(ApiResponse.success("Order accepted for delivery", delivery));
    }

    @PutMapping("/{deliveryId}/status")
    public ResponseEntity<ApiResponse<DeliveryResponse>> updateDeliveryStatus(
            @PathVariable Long deliveryId,
            @Valid @RequestBody DeliveryStatusUpdateRequest request) {
        DeliveryResponse delivery = deliveryService.updateDeliveryStatus(deliveryId, request);
        return ResponseEntity.ok(ApiResponse.success("Delivery status updated", delivery));
    }

    @GetMapping("/my-deliveries/{userId}")
    public ResponseEntity<ApiResponse<List<DeliveryResponse>>> getMyDeliveries(@PathVariable Long userId) {
        List<DeliveryResponse> deliveries = deliveryService.getDeliveriesByPartner(userId);
        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }
}
