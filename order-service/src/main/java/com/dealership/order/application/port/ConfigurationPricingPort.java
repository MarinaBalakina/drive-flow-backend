package com.dealership.order.application.port;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public interface ConfigurationPricingPort {
    BigDecimal calculateTotalPrice(String modelKey, Map<String, UUID> selectedOptionsId);
}
