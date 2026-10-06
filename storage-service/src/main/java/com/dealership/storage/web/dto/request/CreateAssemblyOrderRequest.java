package com.dealership.storage.web.dto.request;

import com.dealership.storage.domain.entity.assembly.AssemblyOrderStatus;
import com.dealership.storage.domain.entity.assembly.AssemblySourceOrderType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CreateAssemblyOrderRequest (
        @NotNull UUID sourceOrderId,
        @NotNull AssemblySourceOrderType orderType,
        UUID carId,
        UUID carModelId,
        Set<UUID>requiredComponentOptionIds,
        @NotNull UUID warehouseEmployeeId,
        String traceId
) {
}
