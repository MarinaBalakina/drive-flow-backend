package com.dealership.order.infrastructure.configurator;

import com.dealership.order.application.port.ConfigurationPricingPort;
import com.dealership.order.domain.exception.DomainValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalConfigurationPricingCatalog implements ConfigurationPricingPort {
    private static final Set<ComponentType> REQUIRED_TYPES = Set.of(ComponentType.values());
    private static final Map<String, ModelDefinition> MODELS = models();
    private static final Map<UUID, OptionDefinition> OPTIONS = options();

    @Override
    public BigDecimal calculateTotalPrice(String modelKey, Map<String, UUID> selectedOptionsId) {
        if (modelKey == null || modelKey.isBlank()) {
            throw new DomainValidationException("Model key must be not empty");
        }

        if (selectedOptionsId == null || selectedOptionsId.isEmpty()) {
            throw new DomainValidationException("Selected options must be not empty");
        }

        ModelDefinition model = MODELS.get(modelKey.trim());
        if (model == null) {
            throw new DomainValidationException("Car model " + modelKey + " not found");
        }

        Map<ComponentType, OptionDefinition> selected = resolveSelectedOptions(model, selectedOptionsId);
        REQUIRED_TYPES.stream()
                .filter(type -> !selected.containsKey(type))
                .findFirst()
                .ifPresent(type -> {
                    throw new DomainValidationException("Missing required component: " + type);
                });

        BigDecimal optionsTotal = selected.values().stream()
                .map(OptionDefinition::priceChange)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return model.basePrice().add(optionsTotal);
    }

    private Map<ComponentType, OptionDefinition> resolveSelectedOptions(
            ModelDefinition model,
            Map<String, UUID> selectedOptionsId
    ) {
        Map<ComponentType, OptionDefinition> selected = new EnumMap<>(ComponentType.class);
        selectedOptionsId.forEach((rawType, optionId) -> {
            ComponentType type = parseType(rawType);
            OptionDefinition option = OPTIONS.get(optionId);

            if (option == null) {
                throw new DomainValidationException("Not found option by id: " + optionId);
            }

            if (option.type() != type) {
                throw new DomainValidationException(
                        "Option " + optionId + " does not belong to component type: " + type
                );
            }

            if (!option.compatibleModels().contains(model.key())) {
                throw new DomainValidationException(
                        "Current component: " + type + " " + option.name()
                                + " not available for model: " + model.key()
                );
            }

            selected.put(type, option);
        });
        return selected;
    }

    private ComponentType parseType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            throw new DomainValidationException("Component type must be not empty");
        }

        try {
            return ComponentType.valueOf(rawType.trim().replace("-", "_").toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DomainValidationException("Unknown component type: " + rawType);
        }
    }

    private static Map<String, ModelDefinition> models() {
        return Map.of(
                "Porsche 911 Carrera 4 GTS",
                new ModelDefinition("Porsche 911 Carrera 4 GTS", new BigDecimal("18500000.00")),
                "Mercedes-AMG C 63 S E Performance",
                new ModelDefinition("Mercedes-AMG C 63 S E Performance", new BigDecimal("12900000.00"))
        );
    }

    private static Map<UUID, OptionDefinition> options() {
        String porsche = "Porsche 911 Carrera 4 GTS";
        String mercedes = "Mercedes-AMG C 63 S E Performance";

        return Map.of(
                uuid("44444444-4444-4444-4444-444444444441"),
                option(ComponentType.WHEELS, "17in Standard", "0.00", porsche, mercedes),
                uuid("44444444-4444-4444-4444-444444444442"),
                option(ComponentType.WHEELS, "19in M Sport", "95000.00", porsche),
                uuid("44444444-4444-4444-4444-444444444443"),
                option(ComponentType.TRANSMISSION, "Automatic 8AT", "0.00", porsche, mercedes),
                uuid("44444444-4444-4444-4444-444444444444"),
                option(ComponentType.TRANSMISSION, "Manual 6MT", "-30000.00", porsche, mercedes),
                uuid("44444444-4444-4444-4444-444444444445"),
                option(ComponentType.STEERING_WHEEL, "Sport Leather Standard", "0.00", porsche, mercedes),
                uuid("44444444-4444-4444-4444-444444444446"),
                option(ComponentType.STEERING_WHEEL, "M Sport Heated", "25000.00", porsche),
                uuid("44444444-4444-4444-4444-444444444447"),
                option(ComponentType.INTERIOR, "Fabric Graphite", "0.00", porsche, mercedes),
                uuid("44444444-4444-4444-4444-444444444448"),
                option(ComponentType.INTERIOR, "Leather Dakota", "110000.00", porsche)
        );
    }

    private static UUID uuid(String value) {
        return UUID.fromString(value);
    }

    private static OptionDefinition option(ComponentType type, String name, String priceChange, String... compatibleModels) {
        return new OptionDefinition(type, name, new BigDecimal(priceChange), Set.of(compatibleModels));
    }

    private enum ComponentType {
        WHEELS,
        TRANSMISSION,
        STEERING_WHEEL,
        INTERIOR
    }

    private record ModelDefinition(String key, BigDecimal basePrice) {
    }

    private record OptionDefinition(
            ComponentType type,
            String name,
            BigDecimal priceChange,
            Set<String> compatibleModels
    ) {
    }
}
