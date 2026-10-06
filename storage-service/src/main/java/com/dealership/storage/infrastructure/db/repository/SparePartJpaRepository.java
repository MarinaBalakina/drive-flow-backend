package com.dealership.storage.infrastructure.db.repository;

import com.dealership.storage.infrastructure.db.entity.SparePartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SparePartJpaRepository extends JpaRepository<SparePartJpaEntity, UUID> {
    Optional<SparePartJpaEntity> findByIdAndRemovedFalse(UUID id);

    Optional<SparePartJpaEntity> findByComponentOptionIdAndRemovedFalse(UUID componentOptionId);

    List<SparePartJpaEntity> findAllByRemovedFalse();

    List<SparePartJpaEntity> findAllByManufacturerIgnoreCaseAndRemovedFalse(String manufacturer);

    List<SparePartJpaEntity> findAllByNameContainingIgnoreCaseAndRemovedFalse(String namePart);
}
