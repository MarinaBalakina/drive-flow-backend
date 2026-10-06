package com.dealership.storage.web.dto.request;

import com.dealership.storage.domain.entity.assembly.AssemblyOrderStatus;

import java.util.UUID;

public record UpdateAssemblyOrderRequest(
        UUID warehouseEmployeeId,
        AssemblyOrderStatus status
) {
}
