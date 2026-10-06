package com.dealership.storage.infrastructure.db.repository;

import com.dealership.storage.domain.entity.configurator.ComponentType;
import com.dealership.storage.infrastructure.db.entity.BaseComponentOptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BaseComponentOptionJpaRepository extends JpaRepository<BaseComponentOptionJpaEntity, UUID> {
    List<BaseComponentOptionJpaEntity> findByBaseConfigurationIdAndComponentType(
            UUID baseConfigurationId,
            ComponentType componentType
    );

    List<BaseComponentOptionJpaEntity> findByBaseConfigurationId(UUID baseConfigurationId);

    void deleteByBaseConfigurationId(UUID baseConfigurationId);
}
