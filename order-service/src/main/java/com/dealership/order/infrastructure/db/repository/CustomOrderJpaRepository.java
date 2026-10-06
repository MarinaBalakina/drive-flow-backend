package com.dealership.order.infrastructure.db.repository;

import com.dealership.order.domain.entity.order.CustomOrderStatus;
import com.dealership.order.infrastructure.db.entity.CustomOrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomOrderJpaRepository extends JpaRepository<CustomOrderJpaEntity, UUID> {
    Optional<CustomOrderJpaEntity> findByIdAndRemovedFalse(UUID id);

    List<CustomOrderJpaEntity> findAllByRemovedFalse();

    List<CustomOrderJpaEntity> findAllByCustomerIdAndRemovedFalse(UUID customerId);

    List<CustomOrderJpaEntity> findAllByManagerIdAndRemovedFalse(UUID managerId);

    List<CustomOrderJpaEntity> findAllByStatusAndRemovedFalse(CustomOrderStatus status);
}
