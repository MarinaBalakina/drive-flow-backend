package com.dealership.storage.infrastructure.db.entity;

import com.dealership.storage.domain.entity.configurator.ComponentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ComponentOptions")
@Getter
@Setter
public class ComponentOptionJpaEntity extends BaseJpaEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "ComponentType", nullable = false)
    private ComponentType componentType;

    @Column(name = "AdditionalPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal additionalPrice;

    @Column(name = "Name", nullable = false)
    private String name;
}
