package com.dealership.storage.infrastructure.db.entity;

import com.dealership.storage.domain.entity.configurator.ComponentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "BaseComponentOptions")
@Getter
@Setter
public class BaseComponentOptionJpaEntity {

    @Id
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BaseConfigurationId", nullable = false)
    private BaseConfigurationJpaEntity baseConfiguration;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ComponentOptionId", nullable = false)
    private ComponentOptionJpaEntity componentOption;

    @Enumerated(EnumType.STRING)
    @Column(name = "ComponentType", nullable = false)
    private ComponentType componentType;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }
}
