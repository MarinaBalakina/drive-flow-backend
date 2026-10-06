package com.dealership.order.application.port;

import com.dealership.order.application.readmodel.AvailableCar;

import java.util.List;
import java.util.UUID;

public interface AvailableCarCatalogPort {
    List<AvailableCar> getAvailableCars();

    AvailableCar getAvailableCarById(UUID id);
}
