package com.dealership.storage.domain.entity.car;

import com.dealership.storage.domain.exception.DomainValidationException;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class Car {
    private final UUID id;
    private BigDecimal price;
    private String brand;
    private String model;
    private CarDetails details;
    private CarStatus status;
    private boolean testDriveEnabled;

    public Car(UUID id, BigDecimal price, String brand, String model,
               CarDetails details, CarStatus status, boolean testDriveEnabled) {
        if (id == null)
            throw new DomainValidationException("Car id must be not null");

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
            throw new DomainValidationException("Price must be greater then 0");

        if (brand == null || brand.trim().isEmpty())
            throw new DomainValidationException("Brand must be not empty");

        if (model == null || model.trim().isEmpty())
            throw new DomainValidationException("Model must be not empty");

        if (details == null)
            throw new DomainValidationException("Car details must be not empty");

        if (status == null)
            throw new DomainValidationException("Car status must be not null");

        this.id = id;
        this.price = price;
        this.brand = brand.trim();
        this.model = model.trim();
        this.details = details;
        this.status = status;
        this.testDriveEnabled = testDriveEnabled && status == CarStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return status == CarStatus.AVAILABLE;
    }

    public void enableTestDrive() {
        if (isAvailable()) testDriveEnabled = true;
        else throw new DomainValidationException("Cannot enable test drive for unavailable car");
    }

    public void disableTestDrive() {
        testDriveEnabled = false;
    }

    public void markSold() {
        status = CarStatus.SOLD;
        disableTestDrive();
    }

    public void markReserved() {
        if (!isAvailable())
            throw new DomainValidationException("Car is unavailable");

        status = CarStatus.RESERVED;
        disableTestDrive();
    }

    public void releaseReservation() {
        if (status != CarStatus.RESERVED)
            throw new DomainValidationException("Only reserved car can be released");

        status = CarStatus.AVAILABLE;
    }

    public String getFullModelName() {
        String b = brand == null ? "" : brand.trim();
        String m = model == null ? "" : model.trim();

        if (b.isEmpty()) return m;

        String prefix = b + " ";
        if (m.regionMatches(true, 0, prefix, 0, prefix.length())) return m;

        return prefix + m;
    }

    public void updateInfo(BigDecimal newPrice, String newBrand, String newModel,
                           CarDetails newDetails, CarStatus status) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new DomainValidationException("Price must be greater then 0");

        if (newBrand == null || newBrand.trim().isEmpty())
            throw new DomainValidationException("Brand must be not empty");

        if (newModel == null || newModel.trim().isEmpty())
            throw new DomainValidationException("Model must be not empty");

        if (newDetails == null)
            throw new DomainValidationException("Car details must be not empty");

        if (status == null)
            throw new DomainValidationException("Car status must be not null");

        price = newPrice;
        brand = newBrand.trim();
        model = newModel.trim();
        details = newDetails;
        this.status = status;

        if (!isAvailable())
            disableTestDrive();
    }
}

