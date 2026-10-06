package com.dealership.storage.web.dto.response;

import com.dealership.storage.domain.entity.car.CarStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CarResponse(
    UUID id,
    BigDecimal price,
    String brand,
    String model,
    CarStatus status,
    boolean testDriveEnabled,
    CarDetailsResponse details

){}
