package com.campusfood.dto.request;

import com.campusfood.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryStatusUpdateRequest {

    @NotNull(message = "Delivery status is required")
    private DeliveryStatus status;
}
