package com.dealership.storage.domain.entity.configurator;

import com.dealership.storage.domain.exception.DomainValidationException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class SelectedConfiguration {
    private final @NonNull Map<ComponentType, ComponentOption> selected;

    public static SelectedConfiguration of(Map<ComponentType, ComponentOption> selected){
        if (selected == null || selected.isEmpty())
            throw new DomainValidationException("Selected component must be not empty");

        if (selected.keySet().stream().anyMatch(Objects::isNull))
            throw new DomainValidationException("Component type must be not null");

        if (selected.values().stream().anyMatch(Objects::isNull))
            throw new DomainValidationException("Component option must be not null");

        Map<ComponentType, ComponentOption> copy = new EnumMap<>(ComponentType.class);
        copy.putAll(selected);
        return new SelectedConfiguration(Collections.unmodifiableMap(copy));
    }

    public ComponentOption get(ComponentType type){
        if (type == null)
            throw new DomainValidationException("Component type must be not null");

        return selected.get(type);
    }
}

