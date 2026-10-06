package com.dealership.storage.web.dto.response;

public record CarDetailsResponse (
        String bodyType,
        String fuelType,
        int enginePower,
        double engineCapacity,
        String transmission,
        String driveType,
        String color
){}
