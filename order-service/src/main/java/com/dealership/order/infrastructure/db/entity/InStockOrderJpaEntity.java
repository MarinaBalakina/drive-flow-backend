package com.dealership.order.infrastructure.db.entity;

import com.dealership.order.domain.entity.order.InStockOrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "InStockOrders")
@Getter
@Setter
public class InStockOrderJpaEntity extends BaseJpaEntity {
    @Column(name = "CustomerId", nullable = false)
    private UUID customerId;

    @Column(name = "ManagerId")
    private UUID managerId;

    @Column(name = "CarId", nullable = false)
    private UUID carId;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private InStockOrderStatus status;

    @Column(name = "FinalPrice", precision = 10, scale = 2)
    private BigDecimal finalPrice;
}
