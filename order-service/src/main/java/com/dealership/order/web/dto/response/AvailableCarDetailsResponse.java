package com.dealership.order.web.dto.response;

public record AvailableCarDetailsResponse(
        String bodyType,
        String fuelType,
        int enginePower,
        double engineCapacity,
        String transmission,
        String driveType,
        String color
){}
