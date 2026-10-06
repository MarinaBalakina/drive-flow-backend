package com.dealership.storage.infrastructure.db.entity;

import com.dealership.storage.domain.entity.assembly.AssemblyOrderStatus;
import com.dealership.storage.domain.entity.assembly.AssemblySourceOrderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "AssemblyOrder")
@Getter
@Setter
public class AssemblyOrderJpaEntity extends BaseJpaEntity {
    @Column(name = "SourceOrderId", nullable = false)
    private UUID sourceOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "SourceOrderType", nullable = false)
    private AssemblySourceOrderType sourceOrderType;

    @Column(name = "CarId")
    private UUID carId;

    @Column(name = "CarModelId")
    private UUID carModelId;

    @ElementCollection
    @CollectionTable(
            name = "AssemblyOrder_RequiredComponents",
            joinColumns = @JoinColumn(name = "AssemblyOrderId")
    )
    @Column(name = "RequiredComponentOptionId")
    private Set<UUID> requiredComponentOptionIds;

    @Column(name = "WarehouseEmployeeId", nullable = false)
    private UUID warehouseEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private AssemblyOrderStatus orderStatus;

    @Column(name = "TraceId", nullable = false)
    private String traceId;
}
