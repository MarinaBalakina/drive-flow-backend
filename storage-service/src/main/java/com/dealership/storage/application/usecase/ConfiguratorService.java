package com.dealership.storage.application.usecase;

import com.dealership.storage.application.port.CarModelRepository;
import com.dealership.storage.domain.entity.configurator.CarModel;
import com.dealership.storage.domain.entity.configurator.ComponentOption;
import com.dealership.storage.domain.entity.configurator.ComponentType;
import com.dealership.storage.domain.entity.configurator.SelectedConfiguration;
import com.dealership.storage.domain.exception.DomainValidationException;
import com.dealership.storage.domain.exception.EntityNotFoundException;
import com.dealership.storage.domain.service.CarConfigurator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
public class ConfiguratorService {
    private final @NonNull CarModelRepository modelRepository;
    private final  @NonNull CarConfigurator carConfigurator;

    public SelectedConfiguration buildConfiguration(String modelKey, Map<ComponentType, UUID> selectedOptionsId) {
        if (modelKey == null || modelKey.trim().isEmpty())
            throw new DomainValidationException("Model key must be not empty");

        if (selectedOptionsId == null)
            throw new DomainValidationException("Selected options must be not null");

        CarModel model = getModelByKey(modelKey);
        Map<ComponentType, ComponentOption> chosen = resolveChosenOptions(model, selectedOptionsId);

        return carConfigurator.buildConfiguration(model, chosen);
    }

    public BigDecimal calculateTotalPrice(String modelKey, SelectedConfiguration selectedConfigurator) {
        if (modelKey == null || modelKey.trim().isEmpty())
            throw new DomainValidationException("Model key must be not empty");

        if (selectedConfigurator == null)
            throw new DomainValidationException("Configurator must be not null");

        CarModel model = getModelByKey(modelKey);

        return carConfigurator.calculateTotalPrice(model, selectedConfigurator);
    }

    private CarModel getModelByKey(String modelKey){
        return modelRepository.findByKeyModel(modelKey).orElseThrow(() -> new EntityNotFoundException
                        ("Car model " + modelKey + " not found"));
    }

    private Map<ComponentType, ComponentOption> resolveChosenOptions(CarModel model, Map<ComponentType, UUID> selectedOptionsId){
        return selectedOptionsId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> resolveOneOption(model, e.getKey(), e.getValue()),
                        (left, right) -> right,
                        () -> new EnumMap<>(ComponentType.class)
                ));
    }

    private ComponentOption resolveOneOption(CarModel model, ComponentType type, UUID optionId){
        if (type == null)
            throw new DomainValidationException("Component type must be not null");

        if (optionId == null)
            throw new DomainValidationException(
                    "Option id must be not null for type: " + type);

        ComponentOption option = model.findOptionById(optionId);

        if (option.getType() != type) {
            throw new DomainValidationException(
                    "Option " + optionId + " does not belong to component type: " + type
            );
        }

        return option;
    }
}

