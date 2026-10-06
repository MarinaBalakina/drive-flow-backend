package com.dealership.order.infrastructure.db.entity;

import com.dealership.order.domain.entity.order.CustomOrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "CustomOrder")
@Getter
@Setter
public class CustomOrderJpaEntity extends BaseJpaEntity{
    @Column(name = "CustomerId", nullable = false)
    private UUID customerId;

    @Column(name = "ManagerId")
    private UUID managerId;

    @Column(name = "CarModelId", nullable = false)
    private UUID carModelId;

    @Column(name = "ModelKey", nullable = false)
    private String modelKey;

    @ElementCollection
    @CollectionTable(
            name = "CustomOrder_SelectedOptions",
            joinColumns = @JoinColumn(name = "CustomOrderId")
    )
    @MapKeyColumn(name = "ComponentType")
    @Column(name = "ComponentOptionId", nullable = false)
    private Map<String, UUID> selectedOptionsId = new LinkedHashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private CustomOrderStatus status;

    @Column(name = "FinalPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;

}

