package com.dealership.order.infrastructure.db.repository;

import com.dealership.order.infrastructure.db.entity.TestDriveRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestDriveRequestJpaRepository extends JpaRepository<TestDriveRequestJpaEntity, UUID> {
    Optional<TestDriveRequestJpaEntity> findByIdAndRemovedFalse(UUID id);

    List<TestDriveRequestJpaEntity> findAllByRemovedFalse();

    List<TestDriveRequestJpaEntity> findAllByCustomerIdAndRemovedFalse(UUID customerId);

    List<TestDriveRequestJpaEntity> findAllByCarIdAndRemovedFalse(UUID carId);

    List<TestDriveRequestJpaEntity> findAllByStartAtBetweenAndRemovedFalse(LocalDateTime from, LocalDateTime to);
}