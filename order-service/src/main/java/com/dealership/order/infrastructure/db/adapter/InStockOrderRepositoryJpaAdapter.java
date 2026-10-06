package com.dealership.order.infrastructure.db.adapter;

import com.dealership.order.application.port.InStockOrderRepository;
import com.dealership.order.domain.entity.order.InStockOrder;
import com.dealership.order.domain.exception.DomainValidationException;
import com.dealership.order.infrastructure.db.entity.InStockOrderJpaEntity;
import com.dealership.order.infrastructure.db.repository.InStockOrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class InStockOrderRepositoryJpaAdapter implements InStockOrderRepository {
    private final InStockOrderJpaRepository jpa;

    @Override
    public InStockOrder save(InStockOrder order) {
        if (order == null) {
            throw new DomainValidationException("In stock order must be not null");
        }

        InStockOrderJpaEntity entity = jpa.findById(order.getId()).orElseGet(InStockOrderJpaEntity::new);
        InStockOrderJpaEntity saved = jpa.save(toEntity(order, entity));
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InStockOrder> findById(UUID id) {
        if (id == null) {
            throw new DomainValidationException("In stock order id must be not null");
        }

        return jpa.findByIdAndRemovedFalse(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InStockOrder> findAll() {
        return jpa.findAllByRemovedFalse().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InStockOrder> findAllByClientId(UUID clientId) {
        if (clientId == null) {
            throw new DomainValidationException("Client id must be not null");
        }

        return jpa.findAllByCustomerIdAndRemovedFalse(clientId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID orderId) {
        if (orderId == null) {
            throw new DomainValidationException("In stock order id must be not null");
        }

        jpa.findByIdAndRemovedFalse(orderId).ifPresent(entity -> {
            entity.setRemoved(true);
            jpa.save(entity);
        });
    }

    private InStockOrderJpaEntity toEntity(InStockOrder order, InStockOrderJpaEntity target) {
        target.setId(order.getId());
        target.setCustomerId(order.getClientId());
        target.setManagerId(order.getManagerId());
        target.setStatus(order.getOrderStatus());
        target.setCarId(order.getCarId());

        return target;
    }

    private InStockOrder toDomain(InStockOrderJpaEntity entity) {
        if (entity.getCustomerId() == null || entity.getCarId() == null) {
            throw new DomainValidationException("Broken in-stock order links in DB: " + entity.getId());
        }

        LocalDateTime createdAt = entity.getCreatedAt() == null
                ? LocalDateTime.now(ZoneOffset.UTC)
                : LocalDateTime.ofInstant(entity.getCreatedAt(), ZoneOffset.UTC);

        return new InStockOrder(
                entity.getId(),
                entity.getCustomerId(),
                entity.getManagerId(),
                entity.getCarId(),
                entity.getStatus(),
                createdAt
        );
    }
}
