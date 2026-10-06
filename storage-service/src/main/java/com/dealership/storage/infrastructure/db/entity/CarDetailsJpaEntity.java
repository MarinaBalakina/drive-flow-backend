package com.dealership.storage.infrastructure.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "CarDetails")
@Getter
@Setter
public class CarDetailsJpaEntity {
    @Id
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "BodyType", nullable = false)
    private String bodyType;

    @Column(name = "FuelType", nullable = false)
    private String fuelType;

    @Column(name = "EnginePower", nullable = false)
    private int enginePower;

    @Column(name = "EngineCapacity", nullable = false)
    private BigDecimal engineCapacity;

    @Column(name = "Transmission", nullable = false)
    private String transmission;

    @Column(name = "DriveType")
    private String driveType;

    @Column(name = "Color", nullable = false)
    private String color;
}
