package com.dealership.order.web.controller;

import com.dealership.order.application.readmodel.AvailableCar;
import com.dealership.order.application.readmodel.AvailableCarDetails;
import com.dealership.order.application.usecase.AvailableCarCatalogService;
import com.dealership.order.web.dto.response.AvailableCarDetailsResponse;
import com.dealership.order.web.dto.response.AvailableCarResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {
    private final AvailableCarCatalogService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public List<AvailableCarResponse> getAvailableCars() {
        return service.getAvailableCars().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public AvailableCarResponse getAvailableCarById(@PathVariable UUID id) {
        return toResponse(service.getAvailableCarById(id));
    }

    private AvailableCarResponse toResponse(AvailableCar car) {
        return new AvailableCarResponse(
                car.id(),
                car.price(),
                car.brand(),
                car.model(),
                car.status(),
                car.testDriveEnabled(),
                toResponse(car.details())
        );
    }

    private AvailableCarDetailsResponse toResponse(AvailableCarDetails details) {
        return new AvailableCarDetailsResponse(
                details.bodyType(),
                details.fuelType(),
                details.enginePower(),
                details.engineCapacity(),
                details.transmission(),
                details.driveType(),
                details.color()
        );
    }
}
