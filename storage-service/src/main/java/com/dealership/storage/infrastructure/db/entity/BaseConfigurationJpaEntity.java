package com.dealership.storage.infrastructure.db.entity;

import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "BaseConfigurations")
@Getter
@Setter
public class BaseConfigurationJpaEntity extends BaseJpaEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CarModelId", nullable = false, unique = true)
    private CarModelJpaEntity carModel;
}
