package com.dealership.order.web.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AvailableCarResponse (
        UUID id,
        BigDecimal price,
        String brand,
        String model,
        String status,
        boolean testDriveEnabled,
        AvailableCarDetailsResponse details
){}

