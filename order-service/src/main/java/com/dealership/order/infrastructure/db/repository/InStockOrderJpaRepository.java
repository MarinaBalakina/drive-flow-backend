package com.dealership.order.infrastructure.db.repository;

import com.dealership.order.domain.entity.order.InStockOrderStatus;
import com.dealership.order.infrastructure.db.entity.InStockOrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InStockOrderJpaRepository extends JpaRepository<InStockOrderJpaEntity, UUID> {
    Optional<InStockOrderJpaEntity> findByIdAndRemovedFalse(UUID id);

    List<InStockOrderJpaEntity> findAllByRemovedFalse();

    List<InStockOrderJpaEntity> findAllByCustomerIdAndRemovedFalse(UUID customerId);

    List<InStockOrderJpaEntity> findAllByManagerIdAndRemovedFalse(UUID managerId);

    List<InStockOrderJpaEntity> findAllByStatusAndRemovedFalse(InStockOrderStatus status);

    boolean existsByCarIdAndStatusNotInAndRemovedFalse(UUID carId, Collection<InStockOrderStatus> excludedStatuses);
}
