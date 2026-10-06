package com.dealership.storage.web.dto.response;

import com.dealership.storage.domain.entity.configurator.ComponentType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CarModelResponse (
        UUID id,
        String model,
        BigDecimal basePrice,
        Map<ComponentType, ComponentOptionResponse> baseOptions,
        Map<ComponentType, List<ComponentOptionResponse>> acceptableOptions
){}

