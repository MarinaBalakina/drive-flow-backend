package com.dealership.storage.infrastructure.db.repository;

import com.dealership.storage.infrastructure.db.entity.BaseConfigurationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BaseConfigurationJpaRepository
        extends JpaRepository<BaseConfigurationJpaEntity, UUID>,
        JpaSpecificationExecutor<BaseConfigurationJpaEntity> {
    Optional<BaseConfigurationJpaEntity> findByIdAndRemovedFalse(UUID id);

    List<BaseConfigurationJpaEntity> findAllByRemovedFalse();

    Optional<BaseConfigurationJpaEntity> findByCarModel_IdAndRemovedFalse(UUID carModelId);
}
