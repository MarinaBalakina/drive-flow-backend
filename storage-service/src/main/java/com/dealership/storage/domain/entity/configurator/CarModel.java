package com.dealership.storage.domain.entity.configurator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.dealership.storage.domain.exception.DomainValidationException;
import com.dealership.storage.domain.exception.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CarModel {
    private final @NonNull UUID id;
    private final @NonNull String model;
    private final @NonNull BigDecimal basePrice;
    private final @NonNull Map<ComponentType, ComponentOption> baseOptions;
    private final @NonNull Map<ComponentType, List<ComponentOption>> acceptableOptions;

    public static CarModel of(
            UUID id,
            String model,
            BigDecimal basePrice,
            Map<ComponentType, ComponentOption> baseOptions,
            Map<ComponentType, List<ComponentOption>> acceptableOptions
    ) {
        if (id == null)
            throw new DomainValidationException("Car model id must be not null");

        if (model == null || model.trim().isEmpty())
            throw new DomainValidationException("Car model name must be not empty");

        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new DomainValidationException("Base price must be greater than 0");

        return new CarModel(
                id,
                model.trim(),
                basePrice,
                copyBaseOptions(baseOptions),
                copyAcceptableOptions(acceptableOptions)
        );
    }

    public List<ComponentOption> getOptions(ComponentType type) {
        if (type == null)
            throw new DomainValidationException("Component type must be not null");

        return acceptableOptions.getOrDefault(type, List.of());
    }

    public ComponentOption findOptionById(UUID optionId) {
        if (optionId == null)
            throw new DomainValidationException("Option id must be not null");

        return acceptableOptions.values().stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(option -> option.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Not found option by id"));
    }

    private static Map<ComponentType, ComponentOption> copyBaseOptions(
            Map<ComponentType, ComponentOption> source
    ) {
        if (source == null)
            throw new DomainValidationException("Base options must be not null");

        Map<ComponentType, ComponentOption> copy = new EnumMap<>(ComponentType.class);

        source.forEach((type, option) -> {
            if (type == null)
                throw new DomainValidationException("Base option component type must be not null");

            if (option == null)
                throw new DomainValidationException("Base option must be not null");

            copy.put(type, option);
        });

        return Collections.unmodifiableMap(copy);
    }

    private static Map<ComponentType, List<ComponentOption>> copyAcceptableOptions(
            Map<ComponentType, List<ComponentOption>> source
    ) {
        if (source == null)
            throw new DomainValidationException("Acceptable options must be not null");

        Map<ComponentType, List<ComponentOption>> copy = new EnumMap<>(ComponentType.class);

        source.forEach((type, options) -> {
            if (type == null)
                throw new DomainValidationException("Acceptable option component type must be not null");

            if (options == null)
                throw new DomainValidationException("Acceptable options list must be not null");

            if (options.stream().anyMatch(Objects::isNull))
                throw new DomainValidationException("Acceptable option must be not null");

            copy.put(type, List.copyOf(options));
        });

        return Collections.unmodifiableMap(copy);
    }
}

