package com.dealership.storage.infrastructure.db.repository;

import com.dealership.storage.infrastructure.db.entity.CarModelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarModelJpaRepository extends JpaRepository<CarModelJpaEntity, UUID> {
    Optional<CarModelJpaEntity> findByIdAndRemovedFalse(UUID id);

    List<CarModelJpaEntity> findAllByRemovedFalse();

    Optional<CarModelJpaEntity> findByModelNameAndRemovedFalse(String modelName);

    boolean existsByModelNameAndRemovedFalse(String modelName);
}
