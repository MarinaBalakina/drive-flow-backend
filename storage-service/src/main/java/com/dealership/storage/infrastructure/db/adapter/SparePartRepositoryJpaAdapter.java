package com.dealership.storage.infrastructure.db.adapter;

import com.dealership.storage.application.port.SparePartRepository;
import com.dealership.storage.domain.entity.sparePart.SparePart;
import com.dealership.storage.domain.exception.DomainValidationException;
import com.dealership.storage.infrastructure.db.entity.CarModelJpaEntity;
import com.dealership.storage.infrastructure.db.entity.SparePartCarModelJpaEntity;
import com.dealership.storage.infrastructure.db.entity.SparePartJpaEntity;
import com.dealership.storage.infrastructure.db.repository.CarModelJpaRepository;
import com.dealership.storage.infrastructure.db.repository.SparePartCarModelJpaRepository;
import com.dealership.storage.infrastructure.db.repository.SparePartJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class SparePartRepositoryJpaAdapter implements SparePartRepository {
    private final SparePartJpaRepository jpa;
    private final SparePartCarModelJpaRepository linkJpa;
    private final CarModelJpaRepository carModelJpa;

    @Override
    public SparePart save(SparePart part) {
        if (part == null) {
            throw new DomainValidationException("Spare part must be not null");
        }

        SparePartJpaEntity entity = jpa.findById(part.getId()).orElseGet(SparePartJpaEntity::new);
        entity.setId(part.getId());
        entity.setName(requiredTrim(part.getName(), "Spare part name must be not empty"));
        entity.setPrice(part.getPrice());
        entity.setComponentOptionId(part.getComponentOptionId());
        entity.setQuantity(part.getQuantity());
        entity.setReservedQuantity(part.getReservedQuantity());

        if (entity.getManufacturer() == null || entity.getManufacturer().trim().isEmpty()) {
            entity.setManufacturer("UNKNOWN");
        }

        SparePartJpaEntity saved = jpa.save(entity);
        syncCompatibility(saved, part.getCompatibleModels());

        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SparePart> findById(UUID id) {
        if (id == null) {
            throw new DomainValidationException("Spare part id must be not null");
        }

        return jpa.findByIdAndRemovedFalse(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SparePart> findByComponentOptionId(UUID componentOptionId) {
        if (componentOptionId == null) {
            throw new DomainValidationException("Component option id must be not null");
        }

        return jpa.findByComponentOptionIdAndRemovedFalse(componentOptionId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SparePart> findAll() {
        return jpa.findAllByRemovedFalse().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID partId) {
        if (partId == null) {
            throw new DomainValidationException("Spare part id must be not null");
        }

        jpa.findByIdAndRemovedFalse(partId).ifPresent(entity -> {
            entity.setRemoved(true);
            jpa.save(entity);
        });
    }

    private void syncCompatibility(SparePartJpaEntity sparePart, Set<String> desiredModelsRaw) {
        Set<String> desiredModels = desiredModelsRaw == null
                ? Set.of()
                : desiredModelsRaw.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        List<SparePartCarModelJpaEntity> existingLinks = linkJpa.findAllBySparePart_Id(sparePart.getId());

        Set<String> existingModelNames = existingLinks.stream()
                .map(link -> link.getCarModel().getModelName())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        for (String modelName : desiredModels) {
            if (!existingModelNames.contains(modelName)) {
                CarModelJpaEntity carModel = carModelJpa.findByModelNameAndRemovedFalse(modelName)
                        .orElseThrow(() -> new DomainValidationException("Car model not found: " + modelName));

                SparePartCarModelJpaEntity link = new SparePartCarModelJpaEntity();
                link.setSparePart(sparePart);
                link.setCarModel(carModel);
                linkJpa.save(link);
            }
        }

        for (SparePartCarModelJpaEntity link : existingLinks) {
            String modelName = link.getCarModel().getModelName();
            if (!desiredModels.contains(modelName)) {
                linkJpa.deleteBySparePart_IdAndCarModel_Id(sparePart.getId(), link.getCarModel().getId());
            }
        }
    }

    private SparePart toDomain(SparePartJpaEntity entity) {
        Set<String> compatibleModels = linkJpa.findAllBySparePart_Id(entity.getId()).stream()
                .map(link -> link.getCarModel().getModelName())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        return new SparePart(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                compatibleModels,
                entity.getComponentOptionId(),
                entity.getQuantity(),
                entity.getReservedQuantity()
        );
    }

    private String requiredTrim(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainValidationException(message);
        }
        return value.trim();
    }
}

