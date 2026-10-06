package com.dealership.storage.infrastructure.db.adapter;

import com.dealership.storage.application.port.CarModelRepository;
import com.dealership.storage.domain.entity.configurator.CarModel;
import com.dealership.storage.domain.entity.configurator.ComponentOption;
import com.dealership.storage.domain.entity.configurator.ComponentType;
import com.dealership.storage.domain.exception.DomainValidationException;
import com.dealership.storage.infrastructure.db.entity.BaseComponentOptionJpaEntity;
import com.dealership.storage.infrastructure.db.entity.BaseConfigurationJpaEntity;
import com.dealership.storage.infrastructure.db.entity.CarModelJpaEntity;
import com.dealership.storage.infrastructure.db.entity.ComponentOptionCompatibleModelJpaEntity;
import com.dealership.storage.infrastructure.db.entity.ComponentOptionJpaEntity;
import com.dealership.storage.infrastructure.db.repository.BaseComponentOptionJpaRepository;
import com.dealership.storage.infrastructure.db.repository.BaseConfigurationJpaRepository;
import com.dealership.storage.infrastructure.db.repository.CarModelJpaRepository;
import com.dealership.storage.infrastructure.db.repository.ComponentOptionCompatibleModelJpaRepository;
import com.dealership.storage.infrastructure.db.specification.BaseConfigurationSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class CarModelRepositoryJpaAdapter implements CarModelRepository {
    private final CarModelJpaRepository jpa;
    private final BaseConfigurationJpaRepository baseConfigurationJpaRepository;
    private final BaseComponentOptionJpaRepository baseComponentOptionJpaRepository;
    private final ComponentOptionCompatibleModelJpaRepository componentOptionCompatibleModelJpaRepository;

    @Override
    public CarModel save(CarModel carModel) {
        if (carModel == null) {
            throw new DomainValidationException("Car model must be not null");
        }

        CarModelJpaEntity saved = jpa.save(toEntity(carModel));
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarModel> findById(UUID id) {
        if (id == null) {
            throw new DomainValidationException("Car model id must be not null");
        }

        return jpa.findByIdAndRemovedFalse(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarModel> findAll() {
        return jpa.findAllByRemovedFalse().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarModel> findAllByBaseFilters(String brand, Set<UUID> componentOptionIds) {
        return baseConfigurationJpaRepository.findAll(
                        BaseConfigurationSpecifications.byFilters(brand, componentOptionIds)
                ).stream()
                .map(BaseConfigurationJpaEntity::getCarModel)
                .filter(carModel -> carModel != null && !carModel.isRemoved())
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarModel> findByKeyModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            throw new DomainValidationException("Car model must be not blank");
        }

        return jpa.findByModelNameAndRemovedFalse(model.trim()).map(this::toDomain);
    }

    @Override
    public void deleteById(UUID modelId) {
        if (modelId == null) {
            throw new DomainValidationException("Car model id must be not null");
        }

        jpa.findByIdAndRemovedFalse(modelId).ifPresent(entity -> {
            entity.setRemoved(true);
            jpa.save(entity);
        });
    }

    private CarModelJpaEntity toEntity(CarModel carModel) {
        CarModelJpaEntity entity = jpa.findById(carModel.getId()).orElseGet(CarModelJpaEntity::new);
        entity.setId(carModel.getId());
        entity.setModelName(carModel.getModel());
        entity.setBasePrice(carModel.getBasePrice());
        return entity;
    }

    private CarModel toDomain(CarModelJpaEntity entity) {
        Map<ComponentType, ComponentOption> baseOptions = loadBaseOptions(entity.getId());
        Map<ComponentType, List<ComponentOption>> acceptableOptions = loadAcceptableOptions(entity.getId());

        return CarModel.of(
                entity.getId(),
                entity.getModelName(),
                entity.getBasePrice(),
                baseOptions,
                acceptableOptions
        );
    }

    private Map<ComponentType, ComponentOption> loadBaseOptions(UUID carModelId) {
        Map<ComponentType, ComponentOption> result = new EnumMap<>(ComponentType.class);

        for (BaseComponentOptionJpaEntity link : baseComponentOptionJpaRepository.findAll()) {
            BaseConfigurationJpaEntity baseConfiguration = link.getBaseConfiguration();
            if (baseConfiguration == null || baseConfiguration.isRemoved()) {
                continue;
            }
            if (baseConfiguration.getCarModel() == null) {
                continue;
            }
            if (!carModelId.equals(baseConfiguration.getCarModel().getId())) {
                continue;
            }

            ComponentOptionJpaEntity optionEntity = link.getComponentOption();
            if (optionEntity == null || optionEntity.isRemoved()) {
                continue;
            }

            result.put(link.getComponentType(), toDomainComponent(optionEntity));
        }

        return result;
    }

    private Map<ComponentType, List<ComponentOption>> loadAcceptableOptions(UUID carModelId) {
        Map<ComponentType, List<ComponentOption>> result = new EnumMap<>(ComponentType.class);
        Map<UUID, ComponentOptionJpaEntity> uniqueOptions = new LinkedHashMap<>();

        for (ComponentOptionCompatibleModelJpaEntity link :
                componentOptionCompatibleModelJpaRepository.findByCarModel_Id(carModelId)) {
            ComponentOptionJpaEntity option = link.getComponentOption();
            if (option != null && !option.isRemoved()) {
                uniqueOptions.putIfAbsent(option.getId(), option);
            }
        }

        for (ComponentOptionJpaEntity optionEntity : uniqueOptions.values()) {
            ComponentOption option = toDomainComponent(optionEntity);
            result.computeIfAbsent(option.getType(), t -> new ArrayList<>()).add(option);
        }

        return result;
    }

    private ComponentOption toDomainComponent(ComponentOptionJpaEntity optionEntity) {
        Set<String> compatibleModels = new LinkedHashSet<>();

        for (ComponentOptionCompatibleModelJpaEntity link :
                componentOptionCompatibleModelJpaRepository.findByComponentOption_Id(optionEntity.getId())) {
            if (link.getCarModel() != null && link.getCarModel().getModelName() != null) {
                compatibleModels.add(link.getCarModel().getModelName());
            }
        }

        return new ComponentOption(
                optionEntity.getId(),
                optionEntity.getComponentType(),
                optionEntity.getName(),
                optionEntity.getAdditionalPrice(),
                compatibleModels
        );
    }
}

