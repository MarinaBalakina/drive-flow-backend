package com.dealership.storage.messaging;

import com.dealership.storage.application.port.CarModelRepository;
import com.dealership.storage.domain.entity.assembly.AssemblySourceOrderType;
import com.dealership.storage.domain.exception.DomainValidationException;
import com.dealership.storage.domain.exception.EntityNotFoundException;
import com.dealership.storage.infrastructure.config.RabbitConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderSentForApprovalListener {
    private final CarModelRepository carModelRepository;
    private final ObjectMapper objectMapper;
    private final OrderSentForApprovalHandler orderSentForApprovalHandler;

    @Value("${app.storage.default-warehouse-employee-id:00000000-0000-0000-0000-000000000001}")
    private UUID defaultWarehouseEmployeeId;

    @RabbitListener(queues = RabbitConfig.ORDER_SENT_FOR_APPROVAL_QUEUE)
    public void handle(String payload) {
        String traceId = null;
        UUID eventId = null;
        UUID orderId = null;
        AssemblySourceOrderType orderType = null;
        try {
            OrderSentForApprovalEvent event = objectMapper.readValue(payload, OrderSentForApprovalEvent.class);
            eventId = resolveEventId(event, payload);
            orderId = event.orderId();
            orderType = parseOrderType(event.orderType());
            traceId = normalizeTraceId(event.traceId());
            MDC.put("traceId", traceId);

            try {
                orderSentForApprovalHandler.process(
                        eventId,
                        orderId,
                        orderType,
                        event.carId(),
                        resolveCarModelId(orderType, event),
                        resolveRequiredComponentOptionIds(event),
                        resolveWarehouseEmployeeId(event),
                        traceId
                );
            } catch (Exception e) {
                orderSentForApprovalHandler.reject(eventId, orderId, orderType, traceId);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot handle OrderSentForApproval event", e);
        } finally {
            if (traceId != null) {
                MDC.remove("traceId");
            }
        }
    }

    private UUID resolveEventId(OrderSentForApprovalEvent event, String payload) {
        if (event.eventId() != null) {
            return event.eventId();
        }
        return UUID.nameUUIDFromBytes(payload.getBytes(StandardCharsets.UTF_8));
    }

    private AssemblySourceOrderType parseOrderType(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainValidationException("Order type must be not null");
        }

        String normalized = raw.trim().replace("-", "_").toUpperCase();
        return AssemblySourceOrderType.valueOf(normalized);
    }

    private UUID resolveCarModelId(AssemblySourceOrderType orderType, OrderSentForApprovalEvent event) {
        if (orderType != AssemblySourceOrderType.CUSTOM_ORDER) {
            return event.carModelId();
        }

        if (event.carModelId() != null) {
            return event.carModelId();
        }

        if (event.modelKey() == null || event.modelKey().isBlank()) {
            throw new DomainValidationException("Car model id or model key must be not null");
        }

        return carModelRepository.findByKeyModel(event.modelKey())
                .orElseThrow(() -> new EntityNotFoundException("Car model isn't found"))
                .getId();
    }

    private Set<UUID> resolveRequiredComponentOptionIds(OrderSentForApprovalEvent event) {
        if (event.requiredComponentOptionIds() != null && !event.requiredComponentOptionIds().isEmpty()) {
            return event.requiredComponentOptionIds();
        }

        if (event.selectedOptionsId() == null || event.selectedOptionsId().isEmpty()) {
            return null;
        }

        return new LinkedHashSet<>(event.selectedOptionsId().values());
    }

    private UUID resolveWarehouseEmployeeId(OrderSentForApprovalEvent event) {
        return event.warehouseEmployeeId() == null
                ? defaultWarehouseEmployeeId
                : event.warehouseEmployeeId();
    }

    private String normalizeTraceId(String traceId) {
        return traceId == null || traceId.isBlank() ? UUID.randomUUID().toString() : traceId;
    }

    private record OrderSentForApprovalEvent(
            UUID eventId,
            UUID orderId,
            String orderType,
            UUID carId,
            UUID carModelId,
            String modelKey,
            Set<UUID> requiredComponentOptionIds,
            Map<String, UUID> selectedOptionsId,
            UUID warehouseEmployeeId,
            String traceId
    ) {
    }
}
