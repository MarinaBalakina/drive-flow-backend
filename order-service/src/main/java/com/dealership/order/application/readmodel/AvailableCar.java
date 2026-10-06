package com.dealership.order.application.readmodel;

import java.math.BigDecimal;
import java.util.UUID;

public record AvailableCar(
        UUID id,
        BigDecimal price,
        String brand,
        String model,
        String status,
        boolean testDriveEnabled,
        AvailableCarDetails details) {
}
