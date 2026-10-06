package com.dealership.order.domain.entity.order;

import com.dealership.order.domain.exception.DomainValidationException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class CustomOrder {
    private final @NonNull UUID id;
    private final @NonNull UUID clientId;
    private UUID managerId;
    private final @NonNull UUID carModelId;
    private final @NonNull String fullModel;
    private final @NonNull Map<String, UUID> selectedOptionsId;
    private final @NonNull BigDecimal totalPrice;
    private final @NonNull LocalDateTime createdAt;
    private @NonNull CustomOrderStatus orderStatus;

    public static CustomOrder create(UUID clientId, UUID managerId,
                                     UUID carModelId,
                                     String fullModel,
                                     Map<String, UUID> selectedOptionsId,
                                     BigDecimal totalPrice) {

        if (clientId == null)
            throw new DomainValidationException("Client id must be not null");

        if (fullModel == null || fullModel.trim().isEmpty())
            throw new DomainValidationException("Car model must be not null");

        if (carModelId == null)
            throw new DomainValidationException("Car model id must be not null");

        if (selectedOptionsId == null || selectedOptionsId.isEmpty())
            throw new DomainValidationException("Selected options must be not empty");

        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new DomainValidationException("Total price must be greater than 0");

        CustomOrder order = new CustomOrder(
                UUID.randomUUID(),
                clientId,
                carModelId,
                fullModel.trim(),
                copySelectedOptions(selectedOptionsId),
                totalPrice,
                LocalDateTime.now(),
                CustomOrderStatus.CREATED
        );
        order.managerId = managerId;
        return order;
    }

    public void assignManager(UUID managerId) {
        if (managerId == null)
            throw new DomainValidationException("Manager id must be not null");

        this.managerId = managerId;
    }

    public void markWaitingForPayment() {
        if (orderStatus != CustomOrderStatus.CREATED
                && orderStatus != CustomOrderStatus.APPROVED_BY_WAREHOUSE)
            throw new DomainValidationException("Cannot wait for payment from status " + orderStatus);

        orderStatus = CustomOrderStatus.WAITING_FOR_PAYMENT;
    }

    public void pay() {
        requireStatus(CustomOrderStatus.WAITING_FOR_PAYMENT);
        orderStatus = CustomOrderStatus.PAID;
    }

    public void markWaitingForDelivery() {
        requireStatus(CustomOrderStatus.PAID);
        orderStatus = CustomOrderStatus.WAITING_FOR_DELIVERY;
    }

    public void markReadyForDelivery() {
        requireStatus(CustomOrderStatus.WAITING_FOR_DELIVERY);
        orderStatus = CustomOrderStatus.READY_FOR_DELIVERY;
    }

    public void complete() {
        requireStatus(CustomOrderStatus.READY_FOR_DELIVERY);
        orderStatus = CustomOrderStatus.COMPLETED;
    }

    public void cancel() {
        if (orderStatus != CustomOrderStatus.CREATED
                && orderStatus != CustomOrderStatus.APPROVED_BY_WAREHOUSE
                && orderStatus != CustomOrderStatus.WAITING_FOR_PAYMENT) {
            throw new DomainValidationException("Cannot be canceled from status " +
                    orderStatus);
        }
        orderStatus = CustomOrderStatus.CANCELED;
    }

    public void approvedByStorage(){
        requireStatus(CustomOrderStatus.PAID);
        orderStatus = CustomOrderStatus.READY_FOR_DELIVERY;
    }

    public void rejectByStorage(){
        requireStatus(CustomOrderStatus.PAID);
        orderStatus = CustomOrderStatus.CANCELED;
    }


    private void requireStatus(CustomOrderStatus expected) {
        if (orderStatus != expected)
            throw new DomainValidationException("Cannot change status from "
                    + orderStatus + " to " + expected);
    }

    private static Map<String, UUID> copySelectedOptions(Map<String, UUID> source) {
        Map<String, UUID> copy = new LinkedHashMap<>();
        source.forEach((type, optionId) -> {
            if (type == null || type.trim().isEmpty())
                throw new DomainValidationException("Selected option type must be not empty");

            if (optionId == null)
                throw new DomainValidationException("Selected option id must be not null");

            copy.put(type.trim(), optionId);
        });
        return Collections.unmodifiableMap(copy);
    }
}
