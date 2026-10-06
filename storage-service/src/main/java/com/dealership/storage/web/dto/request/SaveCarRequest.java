package com.dealership.storage.web.dto.request;

import com.dealership.storage.domain.entity.car.CarStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SaveCarRequest (
        UUID id,
        @NotNull @DecimalMin(value = "0.01") BigDecimal price,
        @NotBlank String brand,
        @NotBlank String model,
        @NotNull @Valid SaveCarDetailsRequest details,
        @NotNull CarStatus status,
        @NotNull Boolean testDriveEnabled
){}
