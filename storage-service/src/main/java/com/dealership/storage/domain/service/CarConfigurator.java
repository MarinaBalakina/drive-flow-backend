package com.dealership.storage.domain.service;

import com.dealership.storage.domain.entity.configurator.CarModel;
import com.dealership.storage.domain.entity.configurator.ComponentOption;
import com.dealership.storage.domain.entity.configurator.ComponentType;
import com.dealership.storage.domain.entity.configurator.SelectedConfiguration;
import com.dealership.storage.domain.exception.DomainValidationException;
import com.dealership.storage.domain.exception.IncompatibleComponentException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;


public final class CarConfigurator {
    public SelectedConfiguration buildConfiguration(
            CarModel carModel, Map<ComponentType, ComponentOption> chosen) {
        if (carModel == null)
            throw new DomainValidationException("Car model must be not null");

        if (chosen == null)
            throw new DomainValidationException("Chosen components must be not null");

        if (chosen.keySet().stream().anyMatch(Objects::isNull))
            throw new DomainValidationException("Component type must be not null");

        if (chosen.values().stream().anyMatch(Objects::isNull))
            throw new DomainValidationException("Component option must be not null");

        Arrays.stream(ComponentType.values())
                .filter(type -> !chosen.containsKey(type))
                .findFirst()
                .ifPresent(type -> {
                    throw new DomainValidationException("Missing required component: " + type);

                });

        Map<ComponentType, ComponentOption> finalSelected = new EnumMap<>(ComponentType.class);
        finalSelected.putAll(chosen);

        String model = carModel.getModel();

        finalSelected.entrySet().stream()
                .filter(entry -> !entry.getValue().isCompatibleWith(model))
                .findFirst()
                .ifPresent(entry -> {
                    throw new IncompatibleComponentException(
                            "Current component: " + entry.getKey() + " " + entry.getValue().getName()
                                    + " not available for model: " + model
                    );
                });

        return SelectedConfiguration.of(finalSelected);
    }


    public BigDecimal calculateTotalPrice(CarModel carModel, SelectedConfiguration configuration) {
        if (carModel == null)
            throw new DomainValidationException("Car model must be not null");

        if (configuration == null)
            throw new DomainValidationException("Selected configuration must be not null");

        Map<ComponentType, ComponentOption> selected = configuration.getSelected();
        BigDecimal total = selected.values().stream()
                .map(ComponentOption::getPriceChange)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return carModel.getBasePrice().add(total);
    }
}

