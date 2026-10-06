package com.dealership.storage.application.port;

import com.dealership.storage.domain.entity.car.Car;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarRepository {
    Car save(Car car);

    Optional<Car> findById(UUID id);

    List<Car> findAll();

    void deleteById(UUID carId);

    List<Car> findAllAvailable();
}
