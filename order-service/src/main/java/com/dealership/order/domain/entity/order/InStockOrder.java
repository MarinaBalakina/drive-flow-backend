package com.dealership.order.domain.entity.order;

import com.dealership.order.domain.exception.DomainValidationException;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class InStockOrder {
    private final @NonNull UUID id;
    private final @NonNull UUID clientId;
    private UUID managerId;
    private final @NonNull UUID carId;
    private @NonNull InStockOrderStatus orderStatus;
    private final @NonNull LocalDateTime createdAt;

    public InStockOrder(UUID id,
                        UUID clientId,
                        UUID managerId,
                        UUID carId,
                        InStockOrderStatus orderStatus,
                        LocalDateTime createdAt) {
        if (id == null)
            throw new DomainValidationException("Order id must be not null");

        if (clientId == null)
            throw new DomainValidationException("Client id must be not null");

        if (carId == null)
            throw new DomainValidationException("Car id must be not null");

        if (orderStatus == null)
            throw new DomainValidationException("Order status must be not null");

        if (createdAt == null)
            throw new DomainValidationException("Created at must be not null");

        this.id = id;
        this.clientId = clientId;
        this.managerId = managerId;
        this.carId = carId;
        this.orderStatus = orderStatus;
        this.createdAt = createdAt;
    }

    public static InStockOrder create(UUID clientId, UUID managerId, UUID carId){
        if (clientId == null)
            throw new DomainValidationException("Client id must be not null");

        if (carId == null)
            throw new DomainValidationException("Car id must be not null");

        return new InStockOrder(UUID.randomUUID(), clientId, managerId, carId,
                InStockOrderStatus.CREATED, LocalDateTime.now());
    }

    public void assignManager(UUID managerId) {
        if (managerId == null)
            throw new DomainValidationException("Manager id must be not null");

        this.managerId = managerId;
    }

    public void approvedByManager(){
        if (orderStatus != InStockOrderStatus.CREATED)
            throw new DomainValidationException(
                    "Cannot approve order from status " + orderStatus);

        orderStatus = InStockOrderStatus.APPROVED_BY_MANAGER;
    }

    public void markWaitingForPayment(){
        if (orderStatus != InStockOrderStatus.APPROVED_BY_MANAGER)
            throw new DomainValidationException(
                    "Cannot wait for payment from status " + orderStatus);

        orderStatus = InStockOrderStatus.WAITING_FOR_PAYMENT;
    }

    public void pay(){
        if (orderStatus != InStockOrderStatus.WAITING_FOR_PAYMENT)
            throw new DomainValidationException(
                    "Cannot be paid from status " + orderStatus);

        orderStatus = InStockOrderStatus.PAID;
    }

    public void markReadyForDelivery(){
        if (orderStatus != InStockOrderStatus.PAID)
            throw new DomainValidationException(
                    "Cannot be ready for delivery from status " + orderStatus);

        orderStatus = InStockOrderStatus.READY_FOR_DELIVERY;
    }

    public void complete(){
        if (orderStatus != InStockOrderStatus.READY_FOR_DELIVERY)
            throw new DomainValidationException(
                    "Cannot be completed from status " + orderStatus);

        orderStatus = InStockOrderStatus.COMPLETED;
    }

    public void cancel(){
        if (orderStatus != InStockOrderStatus.CREATED
                && orderStatus != InStockOrderStatus.APPROVED_BY_MANAGER
                && orderStatus != InStockOrderStatus.WAITING_FOR_PAYMENT)
            throw new DomainValidationException(
                    "Cannot be cancelled from status " + orderStatus);

        orderStatus = InStockOrderStatus.CANCELED;
    }

    public void rejectByStorage() {
        if (orderStatus != InStockOrderStatus.PAID)
            throw new DomainValidationException(
                    "Cannot be rejected by storage from status " + orderStatus);

        orderStatus = InStockOrderStatus.CANCELED;
    }
}
