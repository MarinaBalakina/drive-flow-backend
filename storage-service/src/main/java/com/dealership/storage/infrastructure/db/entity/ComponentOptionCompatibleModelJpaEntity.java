package com.dealership.storage.infrastructure.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "ComponentOption_CompatibleModel")
@Getter
@Setter
public class ComponentOptionCompatibleModelJpaEntity {

    @Id
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ComponentOptionId", nullable = false)
    private ComponentOptionJpaEntity componentOption;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CarModelId", nullable = false)
    private CarModelJpaEntity carModel;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }
}
