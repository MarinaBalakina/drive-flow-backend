package com.dealership.storage.web.dto.request;

import com.dealership.storage.domain.entity.configurator.ComponentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CalculatePriceRequest (
    @NotBlank String modelKey,
    @NotNull @NotEmpty Map<@NotNull ComponentType, @NotNull UUID> selectedOptionsId
){}

