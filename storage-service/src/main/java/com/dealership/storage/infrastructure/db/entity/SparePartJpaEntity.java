package com.dealership.storage.infrastructure.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "SpareParts")
@Getter
@Setter
public class SparePartJpaEntity extends BaseJpaEntity {

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "Description")
    private String description;

    @Column(name = "Manufacturer", nullable = false)
    private String manufacturer;

    @Column(name = "ComponentOptionId")
    private UUID componentOptionId;

    @Column(name = "Quantity", nullable = false)
    private int quantity;

    @Column(name = "ReservedQuantity", nullable = false)
    private int reservedQuantity;
}
