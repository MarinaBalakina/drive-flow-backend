package com.dealership.storage.application.filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class CarFilters {
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final String brand;
    private final String model;
    private final String bodyType;
    private final String fuelType;
    private final Integer minEnginePower;
    private final Double minEngineCapacity;
    private final String transmission;
    private final String driveType;
    private final String color;


    public boolean withoutFilters() {
        return this.minPrice == null && this.maxPrice == null &&
                this.brand == null && this.model == null &&
                this.bodyType == null && this.fuelType == null &&
                this.minEnginePower == null && this.minEngineCapacity == null &&
                this.transmission == null && this.driveType == null &&
                this.color == null;
    }
}
