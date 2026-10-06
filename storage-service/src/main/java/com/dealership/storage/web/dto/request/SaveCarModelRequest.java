package com.dealership.storage.web.dto.request;

import com.dealership.storage.domain.entity.configurator.ComponentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SaveCarModelRequest (
        UUID id,
        @NotBlank String model,
        @NotNull @DecimalMin(value = "0.01") BigDecimal basePrice,
        @NotNull @NotEmpty Map<@NotNull ComponentType, @NotNull @Valid SaveComponentOptionRequest> baseOptions,
        @NotNull @NotEmpty Map<@NotNull ComponentType, @NotNull @NotEmpty List<@NotNull @Valid SaveComponentOptionRequest>> acceptableOptions
){}

