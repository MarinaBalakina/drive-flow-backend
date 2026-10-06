package com.dealership.order.infrastructure.configurator;

import com.dealership.order.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalConfigurationPricingCatalogTest {
    private final LocalConfigurationPricingCatalog catalog = new LocalConfigurationPricingCatalog();

    @Test
    void calculatesPriceFromStableOptionIds() {
        BigDecimal totalPrice = catalog.calculateTotalPrice(
                "Porsche 911 Carrera 4 GTS",
                Map.of(
                        "WHEELS", uuid("44444444-4444-4444-4444-444444444442"),
                        "TRANSMISSION", uuid("44444444-4444-4444-4444-444444444443"),
                        "STEERING_WHEEL", uuid("44444444-4444-4444-4444-444444444446"),
                        "INTERIOR", uuid("44444444-4444-4444-4444-444444444448")
                )
        );

        assertEquals(new BigDecimal("18730000.00"), totalPrice);
    }

    @Test
    void rejectsMissingRequiredComponent() {
        Map<String, UUID> options = new LinkedHashMap<>();
        options.put("WHEELS", uuid("44444444-4444-4444-4444-444444444441"));
        options.put("TRANSMISSION", uuid("44444444-4444-4444-4444-444444444443"));
        options.put("STEERING_WHEEL", uuid("44444444-4444-4444-4444-444444444445"));

        assertThrows(DomainValidationException.class, () ->
                catalog.calculateTotalPrice("Porsche 911 Carrera 4 GTS", options)
        );
    }

    @Test
    void rejectsIncompatibleOption() {
        Map<String, UUID> options = Map.of(
                "WHEELS", uuid("44444444-4444-4444-4444-444444444442"),
                "TRANSMISSION", uuid("44444444-4444-4444-4444-444444444443"),
                "STEERING_WHEEL", uuid("44444444-4444-4444-4444-444444444445"),
                "INTERIOR", uuid("44444444-4444-4444-4444-444444444447")
        );

        assertThrows(DomainValidationException.class, () ->
                catalog.calculateTotalPrice("Mercedes-AMG C 63 S E Performance", options)
        );
    }

    private UUID uuid(String value) {
        return UUID.fromString(value);
    }
}
