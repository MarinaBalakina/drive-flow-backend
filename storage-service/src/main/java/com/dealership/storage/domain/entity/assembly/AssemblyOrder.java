package com.dealership.storage.domain.entity.assembly;

import com.dealership.storage.domain.exception.DomainValidationException;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
public class AssemblyOrder {
    private final UUID id;
    private final UUID sourceOrderId;
    private final AssemblySourceOrderType orderType;

    private final UUID carId;
    private final UUID carModelId;
    private final Set<UUID> requiredComponentOptionIds;

    private UUID warehouseEmployeeId;
    private AssemblyOrderStatus orderStatus;
    private final Instant createdAt;
    private Instant updatedAt;
    private final boolean removed;
    private final String traceId;


    public AssemblyOrder(UUID id, UUID sourceOrderId, AssemblySourceOrderType orderType,
                         UUID carId, UUID carModelId, Set<UUID> requiredComponentOptionIds,
                         UUID warehouseEmployeeId, AssemblyOrderStatus status,
                         Instant createdAt, Instant updatedAt, boolean removed, String traceId) {
        if (id == null)
            throw new DomainValidationException("Assembly orders id must be not null");

        if (orderType == null)
            throw new DomainValidationException("Assembly source order status must be not null");

        if (orderType == AssemblySourceOrderType.IN_STOCK_ORDER && carId == null)
            throw new DomainValidationException("In stock order must have car id");

        if (orderType == AssemblySourceOrderType.CUSTOM_ORDER && carModelId == null)
            throw new DomainValidationException("Custom order must have car model id");

        if (orderType == AssemblySourceOrderType.CUSTOM_ORDER &&
                (requiredComponentOptionIds == null ||
                        requiredComponentOptionIds.isEmpty() ||
                        requiredComponentOptionIds.stream().anyMatch(Objects::isNull)))
            throw new DomainValidationException("Component option id must be not null");

        if (warehouseEmployeeId == null)
            throw new DomainValidationException("Warehouse employee id must be not null");

        if (sourceOrderId == null)
            throw new DomainValidationException("Source order id must be not null");

        if (status == null)
            throw new DomainValidationException("Assembly order status must be not null");

        this.id = id;
        this.sourceOrderId = sourceOrderId;
        this.orderType = orderType;
        this.carId = carId;
        this.carModelId = carModelId;
        this.requiredComponentOptionIds = requiredComponentOptionIds == null
                ? Set.of()
                : Set.copyOf(requiredComponentOptionIds);
        this.warehouseEmployeeId = warehouseEmployeeId;
        orderStatus = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.removed = removed;
        this.traceId = normalizeTraceId(traceId);
    }

    public static AssemblyOrder create(UUID sourceOrderId, AssemblySourceOrderType orderType, UUID carId,
                                UUID carModelId, Set<UUID> requiredComponentOptionIds,
                                UUID warehouseEmployeeId, String traceId) {
        if (orderType == null)
            throw new DomainValidationException("Assembly source order type must be not null");

        return switch (orderType) {
            case IN_STOCK_ORDER -> createForInStockOrder(sourceOrderId, carId, warehouseEmployeeId, traceId);

            case CUSTOM_ORDER -> createForCustomOrder(sourceOrderId, carModelId,
                    requiredComponentOptionIds, warehouseEmployeeId, traceId);
        };
    }

    public static AssemblyOrder createForInStockOrder(UUID sourceOrderId, UUID carId,
                                                      UUID warehouseEmployeeId, String traceId) {
        return new AssemblyOrder(UUID.randomUUID(), sourceOrderId, AssemblySourceOrderType.IN_STOCK_ORDER,
                carId, null, null,
                warehouseEmployeeId, AssemblyOrderStatus.CREATED, null, null, false, traceId);
    }

    public static AssemblyOrder createForCustomOrder(UUID sourceOrderId, UUID carModelId,
                                                     Set<UUID> requiredComponentOptionIds,
                                                     UUID warehouseEmployeeId, String traceId) {
        return new AssemblyOrder(UUID.randomUUID(), sourceOrderId, AssemblySourceOrderType.CUSTOM_ORDER,
                null, carModelId, requiredComponentOptionIds,
                warehouseEmployeeId, AssemblyOrderStatus.CREATED, null, null, false, traceId);
    }

    public void assignWarehouseEmployee(UUID warehouseEmployeeId) {
        if (warehouseEmployeeId == null)
            throw new DomainValidationException("Warehouse employee id must be not null");

        this.warehouseEmployeeId = warehouseEmployeeId;
    }

    public void markAssembled() {
        if (orderStatus != AssemblyOrderStatus.CREATED)
            throw new DomainValidationException("Cannot assemble order from status " + orderStatus);

        orderStatus = AssemblyOrderStatus.ASSEMBLED;
    }

    public void fail() {
        if (orderStatus != AssemblyOrderStatus.CREATED)
            throw new DomainValidationException("Cannot fail order from status " + orderStatus);

        orderStatus = AssemblyOrderStatus.FAIL;
    }

    private static String normalizeTraceId(String traceId) {
        return traceId == null || traceId.isBlank() ? UUID.randomUUID().toString() : traceId.trim();
    }
}
