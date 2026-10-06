package com.dealership.order.application.readmodel;

public record AvailableCarDetails (
        String bodyType,
        String fuelType,
        int enginePower,
        double engineCapacity,
        String transmission,
        String driveType,
        String color
){

}
