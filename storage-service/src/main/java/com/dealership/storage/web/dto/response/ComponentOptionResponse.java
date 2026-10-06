package com.dealership.storage.web.dto.response;

import com.dealership.storage.domain.entity.configurator.ComponentType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ComponentOptionResponse (
        UUID id,
        ComponentType type,
        String name,
        BigDecimal priceChange,
        List<String> compatibleModels
){}
