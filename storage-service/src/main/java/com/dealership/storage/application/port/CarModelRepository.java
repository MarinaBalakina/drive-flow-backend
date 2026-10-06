package com.dealership.storage.application.port;

import com.dealership.storage.domain.entity.configurator.CarModel;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CarModelRepository {
    CarModel save(CarModel carModel);

    Optional<CarModel> findById(UUID id);

    List<CarModel> findAll();

    List<CarModel> findAllByBaseFilters(String brand, Set<UUID> componentOptionIds);

    Optional<CarModel> findByKeyModel(String model);

    void deleteById(UUID modelId);
}
