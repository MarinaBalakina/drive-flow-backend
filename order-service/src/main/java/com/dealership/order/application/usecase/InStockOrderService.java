package com.dealership.order.application.usecase;

import com.dealership.order.application.port.InStockOrderRepository;
import com.dealership.order.domain.entity.order.InStockOrder;
import com.dealership.order.domain.entity.order.InStockOrderStatus;
import com.dealership.order.domain.exception.DomainValidationException;
import com.dealership.order.domain.exception.EntityNotFoundException;
import com.dealership.order.infrastructure.security.CurrentUserProvider;
import com.dealership.order.messaging.OrderSentForApprovalPublisher;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
public class InStockOrderService {
    private final @NonNull InStockOrderRepository orderRepository;
    private final @NonNull CurrentUserProvider currentUserProvider;
    private final @NonNull OrderSentForApprovalPublisher orderSentForApprovalPublisher;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public InStockOrder create(UUID clientId, UUID carId) {
        if (clientId == null)
            throw new DomainValidationException("Client id must be not null");

        if (carId == null)
            throw new DomainValidationException("Car id must be not null");

        if (hasActiveOrderForCar(carId))
            throw new DomainValidationException("Car already has active order: " + carId);
        
        InStockOrder order = InStockOrder.create(clientId, null, carId);

        orderRepository.save(order);

        return order;
    }

    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public List<InStockOrder> listAll() {
        if (currentUserProvider.hasRole("MANAGER") || currentUserProvider.hasRole("ADMIN")) {
            return orderRepository.findAll();
        }
        return orderRepository.findAllByClientId(currentUserProvider.getCurrentUserId());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('USER') and @orderSecurity.isInStockOrderOwner(#orderId))")
    public InStockOrder getById(UUID orderId) {
        if (orderId == null)
            throw new DomainValidationException("Order id must be not null");
        return orderRepository.findById(orderId).
                orElseThrow(() -> new EntityNotFoundException(
                        "In stock order not found: " + orderId));

    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public InStockOrder approvedByManager(UUID orderId) {
        InStockOrder order = getById(orderId);
        order.approvedByManager();
        orderRepository.save(order);
        return order;
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public InStockOrder assignManager(UUID orderId, UUID managerId) {
        InStockOrder order = getById(orderId);
        order.assignManager(managerId);
        orderRepository.save(order);
        return order;
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public InStockOrder markWaitingForPayment(UUID orderId) {
        InStockOrder order = getById(orderId);
        order.markWaitingForPayment();
        orderRepository.save(order);
        return order;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('USER') and @orderSecurity.isInStockOrderOwner(#orderId))")
    @Transactional
    public InStockOrder pay(UUID orderId) {
        InStockOrder order = getById(orderId);
        order.pay();
        InStockOrder saved = orderRepository.save(order);
        orderSentForApprovalPublisher.publish(saved, MDC.get("traceId"));
        return saved;
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public InStockOrder complete(UUID orderId) {
        InStockOrder order = getById(orderId);
        order.complete();
        orderRepository.save(order);
        return order;
    }

    @PreAuthorize("hasRole('USER') and @orderSecurity.isInStockOrderOwner(#orderId)")
    public InStockOrder cancel(UUID orderId) {
        InStockOrder order = getById(orderId);
        order.cancel();
        orderRepository.save(order);
        return order;
    }

    public InStockOrder markReadyAfterStorageApproval(UUID orderId) {
        InStockOrder order = getById(orderId);
        order.markReadyForDelivery();
        return orderRepository.save(order);
    }

    public InStockOrder rejectAfterStorageCheck(UUID orderId) {
        InStockOrder order = getById(orderId);
        order.rejectByStorage();
        return orderRepository.save(order);
    }

    private boolean hasActiveOrderForCar(UUID carId) {
        return orderRepository.findAll().stream()
                .anyMatch(order -> order.getCarId().equals(carId) && isActive(order));
    }

    private boolean isActive(InStockOrder order) {
        InStockOrderStatus status = order.getOrderStatus();
        return status != InStockOrderStatus.COMPLETED
                && status != InStockOrderStatus.CANCELED;
    }
}
