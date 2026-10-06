package com.dealership.order.infrastructure.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TestDriveRequest")
@Getter
@Setter
public class TestDriveRequestJpaEntity extends BaseJpaEntity {
    @Column(name = "CustomerId", nullable = false)
    private UUID customerId;

    @Column(name = "CarId", nullable = false)
    private UUID carId;

    @Column(name = "StartAt", nullable = false)
    private LocalDateTime startAt;
}
