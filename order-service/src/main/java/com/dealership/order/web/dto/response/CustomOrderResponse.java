package com.dealership.order.web.dto.response;

import com.dealership.order.domain.entity.order.CustomOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record CustomOrderResponse (
        UUID id,
        UUID clientId,
        UUID managerId,
        UUID carModelId,
        String fullModel,
        Map<String, UUID> selectedOptionsId,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        CustomOrderStatus orderStatus
){}
