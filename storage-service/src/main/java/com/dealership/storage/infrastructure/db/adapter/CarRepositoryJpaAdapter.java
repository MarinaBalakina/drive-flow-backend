package com.dealership.storage.infrastructure.db.adapter;

import com.dealership.storage.application.port.CarRepository;
import com.dealership.storage.domain.entity.car.Car;
import com.dealership.storage.domain.entity.car.CarDetails;
import com.dealership.storage.domain.entity.car.CarStatus;
import com.dealership.storage.domain.exception.DomainValidationException;
import com.dealership.storage.infrastructure.db.entity.CarDetailsJpaEntity;
import com.dealership.storage.infrastructure.db.entity.CarJpaEntity;
import com.dealership.storage.infrastructure.db.entity.CarModelJpaEntity;
import com.dealership.storage.infrastructure.db.repository.CarJpaRepository;
import com.dealership.storage.infrastructure.db.repository.CarModelJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class CarRepositoryJpaAdapter implements CarRepository {
    private final CarJpaRepository jpa;
    private final CarModelJpaRepository carModelJpaRepository;

    @Override
    public Car save(Car car) {
        if (car == null) {
            throw new DomainValidationException("Car must be not null");
        }

        CarJpaEntity entity = jpa.findById(car.getId()).orElseGet(CarJpaEntity::new);
        CarJpaEntity saved = jpa.save(toEntity(car, entity));
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Car> findById(UUID id) {
        if (id == null) {
            throw new DomainValidationException("Car id must be not null");
        }

        return jpa.findByIdAndRemovedFalse(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> findAll() {
        return jpa.findAllByRemovedFalse().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> findAllAvailable() {
        return jpa.findAllByStatusAndRemovedFalse(CarStatus.AVAILABLE).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID carId) {
        if (carId == null) {
            throw new DomainValidationException("Car id must be not null");
        }

        jpa.findByIdAndRemovedFalse(carId).ifPresent(entity -> {
            entity.setRemoved(true);
            jpa.save(entity);
        });
    }

    private CarJpaEntity toEntity(Car car, CarJpaEntity target) {
        target.setId(car.getId());
        target.setPrice(car.getPrice());
        target.setBrand(requiredTrim(car.getBrand(), "Car brand must be not empty"));
        target.setStatus(car.getStatus());
        target.setTestDriveEnabled(car.isTestDriveEnabled());
        target.setCarModel(resolveCarModel(car));
        target.setCarDetails(toDetailsEntity(
                car.getDetails(),
                target.getCarDetails() == null ? new CarDetailsJpaEntity() : target.getCarDetails()
        ));
        return target;
    }

    private Car toDomain(CarJpaEntity entity) {
        CarDetails details = toDomainDetails(entity.getCarDetails());
        String modelName = entity.getCarModel() == null
                ? ""
                : safeTrim(entity.getCarModel().getModelName());

        return new Car(
                entity.getId(),
                entity.getPrice(),
                requiredTrim(entity.getBrand(), "Car brand in DB must be not empty"),
                modelName,
                details,
                entity.getStatus(),
                entity.isTestDriveEnabled()
        );
    }

    private CarModelJpaEntity resolveCarModel(Car car) {
        String fullModelName = requiredTrim(car.getFullModelName(), "Car model must be not empty");
        String rawModelName = requiredTrim(car.getModel(), "Car model must be not empty");

        return carModelJpaRepository.findByModelNameAndRemovedFalse(fullModelName)
                .or(() -> carModelJpaRepository.findByModelNameAndRemovedFalse(rawModelName))
                .orElseThrow(() -> new DomainValidationException(
                        "Car model not found in DB: " + fullModelName
                ));
    }

    private CarDetailsJpaEntity toDetailsEntity(CarDetails details, CarDetailsJpaEntity target) {
        if (details == null) {
            throw new DomainValidationException("Car details must be not null");
        }

        if (target.getId() == null) {
            target.setId(UUID.randomUUID());
        }

        target.setBodyType(requiredTrim(details.getBodyType(), "Body type must be not empty"));
        target.setFuelType(requiredTrim(details.getFuelType(), "Fuel type must be not empty"));
        target.setEnginePower(details.getEnginePower());
        target.setEngineCapacity(java.math.BigDecimal.valueOf(details.getEngineCapacity()));
        target.setTransmission(requiredTrim(details.getTransmission(), "Transmission must be not empty"));
        target.setDriveType(requiredTrim(details.getDriveType(), "Drive type must be not empty"));
        target.setColor(requiredTrim(details.getColor(), "Color must be not empty"));
        return target;
    }

    private CarDetails toDomainDetails(CarDetailsJpaEntity detailsEntity) {
        if (detailsEntity == null) {
            throw new DomainValidationException("Car details in DB must be not null");
        }

        double engineCapacity = detailsEntity.getEngineCapacity() == null
                ? 0.0d
                : detailsEntity.getEngineCapacity().doubleValue();

        return new CarDetails(
                requiredTrim(detailsEntity.getBodyType(), "Body type in DB must be not empty"),
                requiredTrim(detailsEntity.getFuelType(), "Fuel type in DB must be not empty"),
                detailsEntity.getEnginePower(),
                engineCapacity,
                requiredTrim(detailsEntity.getTransmission(), "Transmission in DB must be not empty"),
                detailsEntity.getDriveType() == null ? "" : detailsEntity.getDriveType().trim(),
                requiredTrim(detailsEntity.getColor(), "Color in DB must be not empty")
        );
    }

    private String requiredTrim(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainValidationException(message);
        }
        return value.trim();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}

