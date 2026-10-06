package com.dealership.storage.infrastructure.db.repository;

import com.dealership.storage.domain.entity.configurator.ComponentType;
import com.dealership.storage.infrastructure.db.entity.ComponentOptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComponentOptionJpaRepository extends JpaRepository<ComponentOptionJpaEntity, UUID> {
    Optional<ComponentOptionJpaEntity> findByIdAndRemovedFalse(UUID id);

    List<ComponentOptionJpaEntity> findAllByRemovedFalse();

    List<ComponentOptionJpaEntity> findAllByComponentTypeAndRemovedFalse(ComponentType componentType);

    Optional<ComponentOptionJpaEntity> findByNameIgnoreCaseAndRemovedFalse(String name);
}