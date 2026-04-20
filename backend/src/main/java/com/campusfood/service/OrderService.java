package com.campusfood.service;

import com.campusfood.dto.request.OrderRequest;
import com.campusfood.dto.request.OrderStatusUpdateRequest;
import com.campusfood.dto.response.DeliveryResponse;
import com.campusfood.dto.response.OrderItemResponse;
import com.campusfood.dto.response.OrderResponse;
import com.campusfood.entity.*;
import com.campusfood.enums.OrderStatus;
import com.campusfood.exception.InsufficientStockException;
import com.campusfood.exception.InvalidOperationException;
import com.campusfood.exception.ResourceNotFoundException;
import com.campusfood.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Places a new order with inventory validation and atomic stock deduction.
     */
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PLACED)
                .deliveryAddress(request.getDeliveryAddress())
                .notes(request.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemRequest.getProductId()));

            if (!product.getAvailable()) {
                throw new InvalidOperationException("Product '" + product.getName() + "' is not available");
            }

            // Lock inventory row to prevent race conditions
            Inventory inventory = inventoryRepository.findByProductIdWithLock(product.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for product: " + product.getName()));

            if (inventory.getQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        product.getName(), itemRequest.getQuantity(), inventory.getQuantity());
            }

            // Deduct stock
            inventory.setQuantity(inventory.getQuantity() - itemRequest.getQuantity());
            inventoryRepository.save(inventory);

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .totalPrice(itemTotal)
                    .build();

            order.addItem(orderItem);
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);
        Order saved = orderRepository.save(order);

        log.info("Order #{} placed by user {} — ₹{}", saved.getId(), user.getName(), totalAmount);
        return mapToResponse(saved);
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return mapToResponse(order);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        validateStatusTransition(order.getStatus(), request.getStatus());
        order.setStatus(request.getStatus());

        Order updated = orderRepository.save(order);
        log.info("Order #{} status updated: {} → {}", orderId, order.getStatus(), request.getStatus());
        return mapToResponse(updated);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new InvalidOperationException(
                    "Order can only be cancelled when in PLACED status. Current: " + order.getStatus());
        }

        // Restore inventory
        for (OrderItem item : order.getItems()) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for product: " + item.getProduct().getName()));
            inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
            inventoryRepository.save(inventory);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelled = orderRepository.save(order);

        log.info("Order #{} cancelled — inventory restored", orderId);
        return mapToResponse(cancelled);
    }

    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validates that a status transition follows the state machine rules.
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        boolean valid = switch (current) {
            case PLACED -> target == OrderStatus.PACKING || target == OrderStatus.CANCELLED;
            case PACKING -> target == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> target == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };

        if (!valid) {
            throw new InvalidOperationException(
                    "Invalid status transition: " + current + " → " + target);
        }
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        DeliveryResponse deliveryResponse = null;
        if (order.getDelivery() != null) {
            Delivery d = order.getDelivery();
            deliveryResponse = DeliveryResponse.builder()
                    .id(d.getId())
                    .orderId(order.getId())
                    .deliveryPartnerId(d.getDeliveryPartner() != null ? d.getDeliveryPartner().getId() : null)
                    .deliveryPartnerName(d.getDeliveryPartner() != null ? d.getDeliveryPartner().getName() : null)
                    .status(d.getStatus())
                    .assignedAt(d.getAssignedAt())
                    .pickedUpAt(d.getPickedUpAt())
                    .deliveredAt(d.getDeliveredAt())
                    .build();
        }

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userName(order.getUser().getName())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .notes(order.getNotes())
                .items(itemResponses)
                .delivery(deliveryResponse)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
