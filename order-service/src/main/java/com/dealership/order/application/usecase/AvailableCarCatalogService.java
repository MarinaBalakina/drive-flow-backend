package com.dealership.order.application.usecase;

import com.dealership.order.application.port.AvailableCarCatalogPort;
import com.dealership.order.application.readmodel.AvailableCar;
import com.dealership.order.domain.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class AvailableCarCatalogService {
    private final AvailableCarCatalogPort availableCarCatalogPort;

    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public List<AvailableCar> getAvailableCars() {
        return availableCarCatalogPort.getAvailableCars();
    }

    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public AvailableCar getAvailableCarById (UUID id) {
        if (id == null)
            throw new DomainValidationException("Car id must be not null");

        return availableCarCatalogPort.getAvailableCarById(id);
    }
}
