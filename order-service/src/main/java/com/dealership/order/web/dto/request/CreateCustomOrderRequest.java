package com.dealership.order.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CreateCustomOrderRequest(
        @NotNull UUID carModelId,
        @NotBlank String modelKey,
        @NotNull @NotEmpty Map<@NotBlank String, @NotNull UUID> selectedOptionsId
) {}
