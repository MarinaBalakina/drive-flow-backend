package com.dealership.storage.domain.entity.sparePart;

import com.dealership.storage.domain.exception.DomainValidationException;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class SparePart {
    private final UUID id;
    private String name;
    private BigDecimal price;
    private final Set<String> compatibleModels;
    private UUID componentOptionId;
    private int quantity;
    private int reservedQuantity;

    public SparePart(UUID id, String name, BigDecimal price, Set<String> compatibleModels) {
        this(id, name, price, compatibleModels, null, 0, 0);
    }

    public SparePart(UUID id, String name, BigDecimal price, Set<String> compatibleModels,
                     UUID componentOptionId, int quantity, int reservedQuantity) {
        if (id == null)
            throw new DomainValidationException("Spare part id must be not null");

        validateName(name);
        validatePrice(price);
        validateStock(quantity, reservedQuantity);

        this.id = id;
        this.name = name.trim();
        this.price = price;
        this.compatibleModels = copyCompatibleModels(compatibleModels);
        this.componentOptionId = componentOptionId;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
    }

    public boolean isCompatibleWith(String model) {
        if (model == null || model.trim().isEmpty())
            throw new DomainValidationException("Model must be not empty");

        return compatibleModels.contains(model.trim());
    }

    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    public void addCompatibleModel(String model) {
        if (model == null || model.trim().isEmpty())
            throw new DomainValidationException("Model must be not empty");

        compatibleModels.add(model.trim());
    }

    public void removeCompatibleModel(String model) {
        if (model == null || model.trim().isEmpty())
            throw new DomainValidationException("Model must be not empty");

        compatibleModels.remove(model.trim());
    }

    public void updateInfo(String newName, BigDecimal newPrice) {
        validateName(newName);
        validatePrice(newPrice);

        name = newName.trim();
        price = newPrice;
    }

    public void updateWarehouseAccounting(UUID componentOptionId, int quantity, int reservedQuantity) {
        validateStock(quantity, reservedQuantity);

        this.componentOptionId = componentOptionId;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
    }

    public void reserveOne() {
        if (componentOptionId == null)
            throw new DomainValidationException("Spare part is not linked to configurator option");

        if (getAvailableQuantity() <= 0)
            throw new DomainValidationException("Spare part is out of stock: " + componentOptionId);

        reservedQuantity++;
    }

    public void releaseOneReservation() {
        if (reservedQuantity <= 0)
            throw new DomainValidationException("No reserved spare parts to release: " + componentOptionId);

        reservedQuantity--;
    }

    public void consumeOneReserved() {
        if (reservedQuantity <= 0)
            throw new DomainValidationException("No reserved spare parts to consume: " + componentOptionId);

        reservedQuantity--;
        quantity--;
    }

    private static void validateName(String value) {
        if (value == null || value.trim().isEmpty())
            throw new DomainValidationException("Name must be not empty");
    }

    private static void validatePrice(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0)
            throw new DomainValidationException("Price must be greater than 0");
    }

    private static void validateStock(int quantity, int reservedQuantity) {
        if (quantity < 0)
            throw new DomainValidationException("Quantity must be greater than or equal to 0");

        if (reservedQuantity < 0)
            throw new DomainValidationException("Reserved quantity must be greater than or equal to 0");

        if (reservedQuantity > quantity)
            throw new DomainValidationException("Reserved quantity must be less than or equal to quantity");
    }

    private static Set<String> copyCompatibleModels(Set<String> source) {
        if (source == null)
            throw new DomainValidationException("Compatible models must be not null");

        Set<String> copy = new LinkedHashSet<>();
        source.forEach(model -> {
            if (model == null || model.trim().isEmpty())
                throw new DomainValidationException("Compatible model must be not empty");
            copy.add(model.trim());
        });
        return copy;
    }
}
