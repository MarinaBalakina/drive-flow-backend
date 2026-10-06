package com.dealership.storage.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record SaveSparePartRequest (
        UUID id,
        @NotBlank String name,
        @NotNull @DecimalMin(value = "0.01") BigDecimal price,
        @NotNull Set<@NotBlank String> compatibleModels,
        UUID componentOptionId,
        @PositiveOrZero Integer quantity,
        @PositiveOrZero Integer reservedQuantity
){}
