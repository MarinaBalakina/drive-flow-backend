package com.dealership.storage.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveCarDetailsRequest (
        @NotBlank String bodyType,
        @NotBlank String fuelType,
        @Min(1) int enginePower,
        @NotNull @DecimalMin(value = "0.1") Double engineCapacity,
        @NotBlank String transmission,
        @NotBlank String driveType,
        @NotBlank String color
){}
