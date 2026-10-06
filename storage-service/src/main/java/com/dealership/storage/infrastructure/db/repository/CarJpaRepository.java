package com.dealership.storage.infrastructure.db.repository;

import com.dealership.storage.domain.entity.assembly.AssemblySourceOrderType;
import com.dealership.storage.domain.entity.car.CarStatus;
import com.dealership.storage.infrastructure.db.entity.CarJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarJpaRepository extends JpaRepository<CarJpaEntity, UUID> {
    Optional<CarJpaEntity> findByIdAndRemovedFalse(UUID id);

    List<CarJpaEntity> findAllByRemovedFalse();

    List<CarJpaEntity> findAllByStatusAndRemovedFalse(CarStatus status);

    boolean existsByIdAndRemovedFalse(UUID id);
}
