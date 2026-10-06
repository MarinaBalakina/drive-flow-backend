package com.dealership.storage.infrastructure.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CarModel")
@Getter
@Setter
public class CarModelJpaEntity extends BaseJpaEntity{
    @Column(name = "ModelName", nullable = false)
    private String modelName;

    @Column(name = "BasePrice", nullable = false)
    private BigDecimal basePrice;
}
