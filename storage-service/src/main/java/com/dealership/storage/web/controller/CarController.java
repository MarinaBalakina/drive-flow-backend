package com.dealership.storage.web.controller;

import com.dealership.storage.application.filter.CarFilters;
import com.dealership.storage.application.usecase.CarCatalogService;
import com.dealership.storage.web.dto.response.CarResponse;
import com.dealership.storage.web.mapper.DomainResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
@Validated
public class CarController {
    private final CarCatalogService carCatalogService;
    private final DomainResponseMapper responseMapper;

    @GetMapping("/{carId}")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public CarResponse getById(@PathVariable("carId") UUID carId) {
        return responseMapper.toResponse(carCatalogService.getCarById(carId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public List<CarResponse> listAvailable(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String bodyType,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) Integer minEnginePower,
            @RequestParam(required = false) Double minEngineCapacity,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) String driveType,
            @RequestParam(required = false) String color
    ) {
        CarFilters filters = new CarFilters(
                minPrice,
                maxPrice,
                brand,
                model,
                bodyType,
                fuelType,
                minEnginePower,
                minEngineCapacity,
                transmission,
                driveType,
                color
        );

        return carCatalogService.listAvailableCars(filters).stream()
                .map(responseMapper::toResponse)
                .toList();
    }
}

