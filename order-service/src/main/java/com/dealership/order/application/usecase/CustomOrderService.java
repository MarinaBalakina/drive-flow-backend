package com.dealership.order.application.usecase;

import com.dealership.order.application.port.ConfigurationPricingPort;
import com.dealership.order.application.port.CustomOrderRepository;
import com.dealership.order.domain.entity.order.CustomOrder;
import com.dealership.order.domain.exception.DomainValidationException;
import com.dealership.order.domain.exception.EntityNotFoundException;
import com.dealership.order.infrastructure.security.CurrentUserProvider;
import com.dealership.order.messaging.OrderSentForApprovalPublisher;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class CustomOrderService {
    private final @NonNull CustomOrderRepository orderRepository;
    private final @NonNull CurrentUserProvider currentUserProvider;
    private final @NonNull OrderSentForApprovalPublisher orderSentForApprovalPublisher;
    private final @NonNull ConfigurationPricingPort configurationPricingPort;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public CustomOrder create(
            UUID clientId,
            UUID carModelId,
            String modelKey,
            Map<String, UUID> selectedOptionsId
    ) {
        if (clientId == null)
            throw new DomainValidationException("Client id must be not null");

        if (carModelId == null)
            throw new DomainValidationException("Car model id must be not null");

        if (modelKey == null || modelKey.trim().isEmpty())
            throw new DomainValidationException("Model key must be not null");

        if (selectedOptionsId == null || selectedOptionsId.isEmpty())
            throw new DomainValidationException("Selected options id must be not empty");

        if (selectedOptionsId.keySet().stream().anyMatch(key -> key == null || key.trim().isEmpty()))
            throw new DomainValidationException("Selected option type must be not empty");

        if (selectedOptionsId.values().stream().anyMatch(Objects::isNull))
            throw new DomainValidationException("Selected option id must be not null");

        BigDecimal totalPrice = configurationPricingPort.calculateTotalPrice(modelKey.trim(), selectedOptionsId);

        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new DomainValidationException("Total price must be greater than 0");

        CustomOrder order = CustomOrder.create(
                clientId,
                null,
                carModelId,
                modelKey.trim(),
                selectedOptionsId,
                totalPrice
        );
        orderRepository.save(order);

        return order;
    }

    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public List<CustomOrder> listAll() {
        if (currentUserProvider.hasRole("MANAGER") || currentUserProvider.hasRole("ADMIN")) {
            return orderRepository.findAll();
        }
        return orderRepository.findAllByClientId(currentUserProvider.getCurrentUserId());
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('USER') and @orderSecurity.isCustomOrderOwner(#orderId))")
    public CustomOrder getById(UUID orderId) {
        if (orderId == null)
            throw new DomainValidationException("Order id must be not null");

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Custom order not found: " + orderId));
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public CustomOrder assignManager(UUID orderId, UUID managerId) {
        CustomOrder order = getById(orderId);
        order.assignManager(managerId);
        orderRepository.save(order);
        return order;
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public CustomOrder markWaitingForPayment(UUID orderId) {
        CustomOrder order = getById(orderId);
        order.markWaitingForPayment();
        orderRepository.save(order);
        return order;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('USER') and @orderSecurity.isCustomOrderOwner(#orderId))")
    @Transactional
    public CustomOrder pay(UUID orderId) {
        CustomOrder order = getById(orderId);
        order.pay();
        CustomOrder saved = orderRepository.save(order);
        orderSentForApprovalPublisher.publish(saved, MDC.get("traceId"));
        return saved;
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public CustomOrder complete(UUID orderId) {
        CustomOrder order = getById(orderId);
        order.complete();
        orderRepository.save(order);
        return order;
    }

    @PreAuthorize("hasRole('USER') and @orderSecurity.isCustomOrderOwner(#orderId)")
    public CustomOrder cancel(UUID orderId) {
        CustomOrder order = getById(orderId);
        order.cancel();
        orderRepository.save(order);
        return order;
    }

    public void markReadyAfterStorageApproval(UUID orderId){
        CustomOrder order = getById(orderId);
        order.approvedByStorage();
        orderRepository.save(order);
    }

    public void rejectAfterStorageCheck(UUID orderId){
        CustomOrder order = getById(orderId);
        order.rejectByStorage();
        orderRepository.save(order);
    }
}
