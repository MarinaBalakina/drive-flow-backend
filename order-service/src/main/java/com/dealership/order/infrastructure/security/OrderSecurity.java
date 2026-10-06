package com.dealership.order.infrastructure.security;

import com.dealership.order.application.port.CustomOrderRepository;
import com.dealership.order.application.port.InStockOrderRepository;
import com.dealership.order.application.port.TestDriveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {
    private final CustomOrderRepository customOrderRepository;
    private final InStockOrderRepository inStockOrderRepository;
    private final TestDriveRequestRepository testDriveRequestRepository;
    private final CurrentUserProvider currentUserProvider;

    public boolean isCustomOrderOwner(UUID orderId) {
        UUID current = currentUserProvider.getCurrentUserId();
        return customOrderRepository.findById(orderId)
                .map(o -> o.getClientId().equals(current))
                .orElse(false);
    }

    public boolean isInStockOrderOwner(UUID orderId) {
        UUID current = currentUserProvider.getCurrentUserId();
        return inStockOrderRepository.findById(orderId)
                .map(o -> o.getClientId().equals(current))
                .orElse(false);
    }

    public boolean isTestDriveRequestOwner(UUID requestId) {
        UUID current = currentUserProvider.getCurrentUserId();
        return testDriveRequestRepository.findById(requestId)
                .map(r -> r.getClientId().equals(current))
                .orElse(false);
    }
}
