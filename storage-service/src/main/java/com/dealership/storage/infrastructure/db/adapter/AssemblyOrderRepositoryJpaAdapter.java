package com.dealership.storage.infrastructure.db.adapter;

import com.dealership.storage.application.port.AssemblyOrderRepository;
import com.dealership.storage.domain.entity.assembly.AssemblyOrder;
import com.dealership.storage.domain.entity.assembly.AssemblyOrderStatus;
import com.dealership.storage.domain.entity.assembly.AssemblySourceOrderType;
import com.dealership.storage.domain.exception.DomainValidationException;
import com.dealership.storage.infrastructure.db.entity.AssemblyOrderJpaEntity;
import com.dealership.storage.infrastructure.db.repository.AssemblyOrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
@Transactional
public class AssemblyOrderRepositoryJpaAdapter implements AssemblyOrderRepository {
    private final AssemblyOrderJpaRepository assemblyOrderJpaRepository;

    @Override
    public AssemblyOrder save (AssemblyOrder order) {
        if (order == null)
            throw new DomainValidationException("Assembly order must be not null");

        AssemblyOrderJpaEntity entity = assemblyOrderJpaRepository.
                findById(order.getId()).orElseGet(AssemblyOrderJpaEntity::new);

        AssemblyOrderJpaEntity saved = assemblyOrderJpaRepository.save(toJpaEntity(order, entity));
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AssemblyOrder> findById(UUID assemblyOrderId) {
        if (assemblyOrderId == null)
            throw new DomainValidationException("Assembly order id must be not null");

        return assemblyOrderJpaRepository.findByIdAndRemovedFalse(assemblyOrderId).map(this::toDomain);

    }

    @Override
    @Transactional(readOnly = true)
    public List<AssemblyOrder> findAll() {
        return assemblyOrderJpaRepository.findAllByRemovedFalse().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssemblyOrder> findAllBySourceOrderId(UUID sourceOrderId) {
        if (sourceOrderId == null)
            throw new DomainValidationException("Assembly source order id must be not null");

        return assemblyOrderJpaRepository.findAllBySourceOrderIdAndRemovedFalse(sourceOrderId).stream().map(this::toDomain).toList();

    }

    @Override
    @Transactional(readOnly = true)
    public List<AssemblyOrder> findAllByAssemblyOrderStatus(AssemblyOrderStatus status) {
        if (status == null)
            throw new DomainValidationException("Assembly order status must be not null");

        return assemblyOrderJpaRepository.findAllByOrderStatusAndRemovedFalse(status).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySourceOrderIdAndOrderType(UUID sourceOrderId, AssemblySourceOrderType orderType) {
        if (sourceOrderId == null)
            throw new DomainValidationException("Assembly source order id must be not null");

        if (orderType == null)
            throw new DomainValidationException("Assembly source order type must be not null");

        return assemblyOrderJpaRepository.existsBySourceOrderIdAndSourceOrderTypeAndRemovedFalse(sourceOrderId, orderType);
    }

    @Override
    public void deleteById(UUID orderId) {
        if (orderId == null)
            throw new DomainValidationException("Assembly order id must be not null");

        assemblyOrderJpaRepository.findByIdAndRemovedFalse(orderId).ifPresent(entity -> {
            entity.setRemoved(true);
            assemblyOrderJpaRepository.save(entity);
        });

    }

    private AssemblyOrder toDomain(AssemblyOrderJpaEntity entity) {
        return new AssemblyOrder(
                entity.getId(),
                entity.getSourceOrderId(),
                entity.getSourceOrderType(),
                entity.getCarId(),
                entity.getCarModelId(),
                entity.getRequiredComponentOptionIds(),
                entity.getWarehouseEmployeeId(),
                entity.getOrderStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isRemoved(),
                entity.getTraceId()
                );
    }

    private AssemblyOrderJpaEntity toJpaEntity(AssemblyOrder order, AssemblyOrderJpaEntity entity) {
        entity.setId(order.getId());
        entity.setSourceOrderId(order.getSourceOrderId());
        entity.setSourceOrderType(order.getOrderType());
        entity.setCarId(order.getCarId());
        entity.setCarModelId(order.getCarModelId());
        entity.setRequiredComponentOptionIds(order.getRequiredComponentOptionIds());
        entity.setWarehouseEmployeeId(order.getWarehouseEmployeeId());
        entity.setOrderStatus(order.getOrderStatus());
        entity.setTraceId(order.getTraceId());

        return entity;
    }
}
