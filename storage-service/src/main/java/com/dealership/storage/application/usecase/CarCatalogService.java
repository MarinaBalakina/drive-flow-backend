package com.dealership.storage.application.usecase;

import com.dealership.storage.application.filter.CarFilters;
import com.dealership.storage.application.port.CarRepository;
import com.dealership.storage.domain.entity.car.Car;
import com.dealership.storage.domain.exception.DomainValidationException;
import com.dealership.storage.domain.exception.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class CarCatalogService {
    private final @NonNull CarRepository carRepository;

    public Car getCarById(UUID carId) {
        if (carId == null)
            throw new DomainValidationException("Car id must be not null");

        Optional<Car> found = carRepository.findById(carId);

        if (found.isEmpty())
            throw new EntityNotFoundException("Car " + carId + " not found");

        return found.get();
    }

    public Car getAvailableCarById(UUID id){
        Car car = getCarById(id);

        if (!car.isAvailable())
            throw new EntityNotFoundException("Available car " + id + " not found");

        return car;
    }
    public List<Car> listAvailableCars(CarFilters filters) {
        if (filters == null || filters.withoutFilters())
            return carRepository.findAllAvailable();

        List<Car> allCars = carRepository.findAll();

        validateFilters(filters);

        Stream<Car> res = allCars.stream().filter(Car::isAvailable);

        if (filters.getMinPrice() != null) {
            BigDecimal lim = filters.getMinPrice();
            res = res.filter(car -> car.getPrice().compareTo(lim) >= 0);
        }

        if (filters.getMaxPrice() != null) {
            BigDecimal lim = filters.getMaxPrice();
            res = res.filter(car -> car.getPrice().compareTo(lim) <= 0);
        }

        if (filters.getBrand() != null) {
            String filter = filters.getBrand().trim();
            res = res.filter(car -> car.getBrand().equalsIgnoreCase(filter));
        }

        if (filters.getModel() != null) {
            String filter = filters.getModel().trim();
            res = res.filter(car -> car.getModel().equalsIgnoreCase(filter));
        }

        if (filters.getBodyType() != null) {
            String filter = filters.getBodyType().trim();
            res = res.filter(car -> car.getDetails().getBodyType().equalsIgnoreCase(filter));
        }

        if (filters.getFuelType() != null) {
            String filter = filters.getFuelType().trim();
            res = res.filter(car -> car.getDetails().getFuelType().equalsIgnoreCase(filter));
        }

        if (filters.getMinEnginePower() != null) {
            Integer lim = filters.getMinEnginePower();
            res = res.filter(car -> car.getDetails().getEnginePower() >= lim);
        }

        if (filters.getMinEngineCapacity() != null) {
            Double lim = filters.getMinEngineCapacity();
            res = res.filter(car -> car.getDetails().getEngineCapacity() >= lim);
        }

        if (filters.getTransmission() != null) {
            String filter = filters.getTransmission().trim();
            res = res.filter(car -> car.getDetails().getTransmission().equalsIgnoreCase(filter));
        }

        if (filters.getDriveType() != null) {
            String filter = filters.getDriveType().trim();
            res = res.filter(car -> car.getDetails().getDriveType().equalsIgnoreCase(filter));
        }

        if (filters.getColor() != null) {
            String filter = filters.getColor().trim();
            res = res.filter(car -> car.getDetails().getColor().equalsIgnoreCase(filter));
        }

        return res.toList();
    }

    private void validateFilters(CarFilters filters) {
        BigDecimal minPrice = filters.getMinPrice();
        BigDecimal maxPrice = filters.getMaxPrice();

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)
            throw new DomainValidationException("Min price must be less than or equal to max price");

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0)
            throw new DomainValidationException("Min price must be greater than or equal to 0");

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0)
            throw new DomainValidationException("Max price must be greater than or equal to 0");

        String model = filters.getModel();
        if (model != null && !model.trim().isEmpty()) {
            String brand = filters.getBrand();
            if (brand == null || brand.trim().isEmpty())
                throw new DomainValidationException("Model filter is allowed only when brand filter is set");
        }
    }
}

