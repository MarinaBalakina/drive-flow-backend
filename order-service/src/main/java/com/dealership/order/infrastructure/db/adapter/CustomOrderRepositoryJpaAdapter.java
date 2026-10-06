package com.dealership.order.infrastructure.db.adapter;

import com.dealership.order.application.port.CustomOrderRepository;
import com.dealership.order.domain.entity.order.CustomOrder;
import com.dealership.order.domain.exception.DomainValidationException;
import com.dealership.order.infrastructure.db.entity.CustomOrderJpaEntity;
import com.dealership.order.infrastructure.db.repository.CustomOrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class CustomOrderRepositoryJpaAdapter implements CustomOrderRepository {

    private final CustomOrderJpaRepository jpa;

    @Override
    public CustomOrder save(CustomOrder order) {
        if (order == null) {
            throw new DomainValidationException("Custom order must be not null");
        }

        CustomOrderJpaEntity entity = jpa.findById(order.getId()).orElseGet(CustomOrderJpaEntity::new);
        CustomOrderJpaEntity saved = jpa.save(toEntity(order, entity));
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomOrder> findById(UUID id) {
        if (id == null) {
            throw new DomainValidationException("Custom order id must be not null");
        }

        return jpa.findByIdAndRemovedFalse(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomOrder> findAll() {
        return jpa.findAllByRemovedFalse().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(UUID orderId) {
        if (orderId == null) {
            throw new DomainValidationException("Custom order id must be not null");
        }

        jpa.findByIdAndRemovedFalse(orderId).ifPresent(e -> {
            e.setRemoved(true);
            jpa.save(e);
        });
    }

    private CustomOrderJpaEntity toEntity(CustomOrder order, CustomOrderJpaEntity target) {
        target.setId(order.getId());
        target.setCustomerId(order.getClientId());
        target.setManagerId(order.getManagerId());
        target.setCarModelId(order.getCarModelId());
        target.setModelKey(order.getFullModel());
        target.setSelectedOptionsId(copySelectedOptions(order.getSelectedOptionsId()));
        target.setFinalPrice(order.getTotalPrice());
        target.setStatus(order.getOrderStatus());

        return target;
    }

    private CustomOrder toDomain(CustomOrderJpaEntity entity) {
        LocalDateTime createdAt = entity.getCreatedAt() == null
                ? LocalDateTime.now(ZoneOffset.UTC)
                : LocalDateTime.ofInstant(entity.getCreatedAt(), ZoneOffset.UTC);

        CustomOrder order = new CustomOrder(
                entity.getId(),
                entity.getCustomerId(),
                entity.getCarModelId(),
                entity.getModelKey(),
                copySelectedOptions(entity.getSelectedOptionsId()),
                entity.getFinalPrice(),
                createdAt,
                entity.getStatus()
        );
        if (entity.getManagerId() != null) {
            order.assignManager(entity.getManagerId());
        }
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomOrder> findAllByClientId(UUID clientId) {
        if (clientId == null) {
            throw new DomainValidationException("Client id must be not null");
        }

        return jpa.findAllByCustomerIdAndRemovedFalse(clientId).stream()
                .map(this::toDomain)
                .toList();
    }

    private LinkedHashMap<String, UUID> copySelectedOptions(Map<String, UUID> selectedOptionsId) {
        return selectedOptionsId == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(selectedOptionsId);
    }
}
