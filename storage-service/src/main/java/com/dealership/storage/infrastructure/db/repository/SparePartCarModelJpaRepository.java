package com.dealership.storage.infrastructure.db.repository;

import com.dealership.storage.infrastructure.db.entity.SparePartCarModelJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SparePartCarModelJpaRepository extends JpaRepository<SparePartCarModelJpaEntity, UUID> {
    List<SparePartCarModelJpaEntity> findAllBySparePart_Id(UUID sparePartId);

    List<SparePartCarModelJpaEntity> findAllByCarModel_Id(UUID carModelId);

    boolean existsBySparePart_IdAndCarModel_Id(UUID sparePartId, UUID carModelId);

    void deleteBySparePart_IdAndCarModel_Id(UUID sparePartId, UUID carModelId);
}