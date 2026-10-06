package com.dealership.storage.web.dto.response;

import com.dealership.storage.domain.entity.assembly.AssemblyOrderStatus;
import com.dealership.storage.domain.entity.assembly.AssemblySourceOrderType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AssemblyOrderResponse(
        UUID id,
        UUID sourceOrderId,
        AssemblySourceOrderType orderType,
        UUID carId,
        UUID carModelId,
        Set<UUID>requiredComponentOptionIds,
        UUID warehouseEmployeeId,
        AssemblyOrderStatus orderStatus,
        Instant createdAt,
        Instant updatedAt,
        boolean removed,
        String traceId
) {
}
