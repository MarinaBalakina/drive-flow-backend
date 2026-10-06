package com.dealership.storage.web.dto.response;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record SparePartResponse (
        UUID id,
        String name,
        BigDecimal price,
        Set<String> compatibleModels,
        UUID componentOptionId,
        int quantity,
        int reservedQuantity,
        int availableQuantity
){}
