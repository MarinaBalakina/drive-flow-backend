package com.dealership.storage.infrastructure.db.repository;

import com.dealership.storage.infrastructure.db.entity.ComponentOptionCompatibleModelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ComponentOptionCompatibleModelJpaRepository
        extends JpaRepository<ComponentOptionCompatibleModelJpaEntity, UUID> {

    List<ComponentOptionCompatibleModelJpaEntity> findByComponentOption_Id(UUID componentOptionId);

    List<ComponentOptionCompatibleModelJpaEntity> findByCarModel_Id(UUID carModelId);

    boolean existsByComponentOption_IdAndCarModel_Id(UUID componentOptionId, UUID carModelId);

    void deleteByComponentOption_IdAndCarModel_Id(UUID componentOptionId, UUID carModelId);
}
