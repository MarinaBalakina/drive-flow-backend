package com.dealership.storage.application.usecase;

import com.dealership.storage.application.port.CarModelRepository;
import com.dealership.storage.application.port.CarRepository;
import com.dealership.storage.application.port.SparePartRepository;
import com.dealership.storage.domain.entity.car.Car;
import com.dealership.storage.domain.entity.configurator.CarModel;
import com.dealership.storage.domain.entity.sparePart.SparePart;
import com.dealership.storage.domain.exception.DomainValidationException;
import com.dealership.storage.domain.exception.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
public class WarehouseService {
    private final @NonNull CarRepository carRepository;
    private final @NonNull SparePartRepository sparePartRepository;
    private final @NonNull CarModelRepository carModelRepository;

    public Car addCar(Car car) {
        if (car == null)
            throw new DomainValidationException("Car must be not null");

        carRepository.save(car);

        return car;
    }

    public Car updateCar(Car car) {
        if (car == null)
            throw new DomainValidationException("Car must be not null");

        carRepository.findById(car.getId()).orElseThrow(() -> new EntityNotFoundException(
                "Cannot found car: " + car.getId()));

        carRepository.save(car);

        return car;
    }

    public List<Car> listAllCars() {
        return carRepository.findAll();
    }

    public CarModel addCarModel(CarModel carModel) {
        if (carModel == null)
            throw new DomainValidationException("Car must be not null");

        carModelRepository.save(carModel);

        return carModel;
    }

    public CarModel updateCarModel(CarModel carModel) {
        if (carModel == null)
            throw new DomainValidationException("Car must be not null");

        carModelRepository.findById(carModel.getId()).orElseThrow(() -> new EntityNotFoundException(
                "Cannot found car: " + carModel.getId()));

        carModelRepository.save(carModel);

        return carModel;
    }

    public List<CarModel> listAllCarModels() {
        return carModelRepository.findAll();
    }

    public List<CarModel> listBaseConfigurations(String brand, Set<UUID> componentOptionIds) {
        Set<UUID> normalizedOptionIds = componentOptionIds == null
                ? Set.of()
                : new LinkedHashSet<>(componentOptionIds);
        return carModelRepository.findAllByBaseFilters(brand, normalizedOptionIds);
    }

    public CarModel getCarModelById(UUID id) {
        if (id == null)
            throw new DomainValidationException("Car model id must be not null");

        return carModelRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException(
                        "Cannot found car: " + id));
    }

    public SparePart addSparePart(SparePart part) {
        if (part == null)
            throw new DomainValidationException("Spare part must be not null");

        sparePartRepository.save(part);

        return part;
    }

    public SparePart updateSparePart(SparePart part) {
        if (part == null)
            throw new DomainValidationException("Spare part must be not null");

        sparePartRepository.findById(part.getId()).orElseThrow(() -> new EntityNotFoundException(
                "Cannot found spare part: " + part.getId()));

        sparePartRepository.save(part);

        return part;
    }

    public List<SparePart> listAllParts() {
        return sparePartRepository.findAll();
    }
}

