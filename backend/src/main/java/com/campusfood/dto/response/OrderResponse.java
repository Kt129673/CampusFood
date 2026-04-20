package com.campusfood.dto.response;

import com.campusfood.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long userId;
    private String userName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String notes;
    private List<OrderItemResponse> items;
    private DeliveryResponse delivery;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
