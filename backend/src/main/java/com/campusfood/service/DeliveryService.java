package com.campusfood.service;

import com.campusfood.dto.request.DeliveryStatusUpdateRequest;
import com.campusfood.dto.response.DeliveryResponse;
import com.campusfood.dto.response.OrderResponse;
import com.campusfood.entity.Delivery;
import com.campusfood.entity.Order;
import com.campusfood.entity.User;
import com.campusfood.enums.DeliveryStatus;
import com.campusfood.enums.OrderStatus;
import com.campusfood.enums.UserRole;
import com.campusfood.exception.InvalidOperationException;
import com.campusfood.exception.ResourceNotFoundException;
import com.campusfood.repository.DeliveryRepository;
import com.campusfood.repository.OrderRepository;
import com.campusfood.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    /**
     * Returns orders that are in PACKING status and ready for delivery pickup.
     */
    public List<OrderResponse> getAvailableOrdersForDelivery() {
        List<Order> packingOrders = orderRepository.findByStatus(OrderStatus.PACKING);

        // Filter out orders that already have a delivery assignment
        return packingOrders.stream()
                .filter(order -> order.getDelivery() == null)
                .map(this::mapOrderToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delivery partner accepts an order for delivery (MVP: first-come, first-served).
     */
    @Transactional
    public DeliveryResponse acceptOrder(Long orderId, Long deliveryPartnerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() != OrderStatus.PACKING) {
            throw new InvalidOperationException("Order is not ready for delivery. Current status: " + order.getStatus());
        }

        if (order.getDelivery() != null) {
            throw new InvalidOperationException("Order #" + orderId + " already has a delivery partner assigned");
        }

        User deliveryPartner = userRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", deliveryPartnerId));

        if (deliveryPartner.getRole() != UserRole.DELIVERY) {
            throw new InvalidOperationException("User is not a delivery partner");
        }

        Delivery delivery = Delivery.builder()
                .order(order)
                .deliveryPartner(deliveryPartner)
                .status(DeliveryStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .build();

        Delivery saved = deliveryRepository.save(delivery);

        // Update order status
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderRepository.save(order);

        log.info("Order #{} accepted by delivery partner: {}", orderId, deliveryPartner.getName());
        return mapToResponse(saved);
    }

    /**
     * Updates delivery status (ASSIGNED → PICKED_UP → DELIVERED).
     */
    @Transactional
    public DeliveryResponse updateDeliveryStatus(Long deliveryId, DeliveryStatusUpdateRequest request) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", deliveryId));

        validateDeliveryStatusTransition(delivery.getStatus(), request.getStatus());

        delivery.setStatus(request.getStatus());

        if (request.getStatus() == DeliveryStatus.PICKED_UP) {
            delivery.setPickedUpAt(LocalDateTime.now());
        } else if (request.getStatus() == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());
            // Also update order status to DELIVERED
            Order order = delivery.getOrder();
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }

        Delivery updated = deliveryRepository.save(delivery);
        log.info("Delivery #{} status updated to: {}", deliveryId, request.getStatus());
        return mapToResponse(updated);
    }

    public List<DeliveryResponse> getDeliveriesByPartner(Long deliveryPartnerId) {
        return deliveryRepository.findByDeliveryPartnerId(deliveryPartnerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateDeliveryStatusTransition(DeliveryStatus current, DeliveryStatus target) {
        boolean valid = switch (current) {
            case ASSIGNED -> target == DeliveryStatus.PICKED_UP;
            case PICKED_UP -> target == DeliveryStatus.DELIVERED;
            case DELIVERED -> false;
        };

        if (!valid) {
            throw new InvalidOperationException(
                    "Invalid delivery status transition: " + current + " → " + target);
        }
    }

    private DeliveryResponse mapToResponse(Delivery delivery) {
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrder().getId())
                .deliveryPartnerId(
                        delivery.getDeliveryPartner() != null ? delivery.getDeliveryPartner().getId() : null)
                .deliveryPartnerName(
                        delivery.getDeliveryPartner() != null ? delivery.getDeliveryPartner().getName() : null)
                .status(delivery.getStatus())
                .assignedAt(delivery.getAssignedAt())
                .pickedUpAt(delivery.getPickedUpAt())
                .deliveredAt(delivery.getDeliveredAt())
                .build();
    }

    private OrderResponse mapOrderToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userName(order.getUser().getName())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
