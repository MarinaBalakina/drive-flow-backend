package com.dealership.storage.domain.entity.car;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CarDetails {
    private final @NonNull String bodyType;
    private final @NonNull String fuelType;
    private final int enginePower;
    private final double engineCapacity;
    private final @NonNull String transmission;
    private final @NonNull String driveType;
    private final @NonNull String color;
}
