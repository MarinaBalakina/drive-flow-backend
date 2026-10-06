package com.dealership.order.web.dto.response;

import com.dealership.order.domain.entity.order.InStockOrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record InStockOrderResponse(
        UUID id,
        UUID clientId,
        UUID managerId,
        UUID carId,
        InStockOrderStatus orderStatus,
        LocalDateTime createdAt
){}
