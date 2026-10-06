package com.dealership.storage.web.dto.request;

import com.dealership.storage.domain.entity.configurator.ComponentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record SaveComponentOptionRequest (
        UUID id,
        @NotNull ComponentType type,
        @NotBlank String name,
        @NotNull BigDecimal priceChange,
        @NotNull Set<@NotBlank String> compatibleModels
){}

