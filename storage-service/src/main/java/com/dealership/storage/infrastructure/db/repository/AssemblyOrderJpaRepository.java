package com.dealership.storage.infrastructure.db.repository;

import com.dealership.storage.domain.entity.assembly.AssemblyOrderStatus;
import com.dealership.storage.domain.entity.assembly.AssemblySourceOrderType;
import com.dealership.storage.infrastructure.db.entity.AssemblyOrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssemblyOrderJpaRepository extends JpaRepository<AssemblyOrderJpaEntity, UUID> {
    Optional<AssemblyOrderJpaEntity> findByIdAndRemovedFalse(UUID id);

    List<AssemblyOrderJpaEntity> findAllByRemovedFalse();

    List<AssemblyOrderJpaEntity> findAllBySourceOrderIdAndRemovedFalse(UUID sourceOrderId);

    List<AssemblyOrderJpaEntity> findAllByOrderStatusAndRemovedFalse(AssemblyOrderStatus orderStatus);

    boolean existsBySourceOrderIdAndSourceOrderTypeAndRemovedFalse(UUID sourceOrderId,
                                                                   AssemblySourceOrderType sourceOrderType);
}

