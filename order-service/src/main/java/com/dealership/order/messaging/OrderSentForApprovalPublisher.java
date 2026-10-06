package com.dealership.order.messaging;

import com.dealership.order.domain.entity.order.CustomOrder;
import com.dealership.order.domain.entity.order.InStockOrder;
import com.dealership.order.outbox.OutboxMessage;
import com.dealership.order.outbox.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderSentForApprovalPublisher {
    private static final String EXCHANGE = "dealership-orders";
    private static final String ROUTING_KEY = "order.sent_for_approval";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void publish(CustomOrder order, String traceId){
        UUID eventId = UUID.randomUUID();
        OrderSentForApprovalEvent event = new OrderSentForApprovalEvent(
                eventId,
                order.getId(),
                "CUSTOM_ORDER",
                null,
                order.getCarModelId(),
                order.getFullModel(),
                new LinkedHashSet<>(order.getSelectedOptionsId().values()),
                order.getSelectedOptionsId(),
                null,
                normalizeTraceId(traceId, eventId)
        );

        saveEvent(event);
    }

    public void publish(InStockOrder order, String traceId){
        UUID eventId = UUID.randomUUID();
        OrderSentForApprovalEvent event = new OrderSentForApprovalEvent(
                eventId,
                order.getId(),
                "IN_STOCK_ORDER",
                order.getCarId(),
                null,
                null,
                null,
                null,
                null,
                normalizeTraceId(traceId, eventId)
        );

        saveEvent(event);
    }

    private void saveEvent(OrderSentForApprovalEvent event) {
        try{
            String payload = objectMapper.writeValueAsString(event);

            OutboxMessage message = new OutboxMessage(
                    EXCHANGE,
                    ROUTING_KEY,
                    "OrderSentForApproval",
                    payload
            );

            outboxRepository.save(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize OrderSentForApproval event", e);
        }
    }

    private String normalizeTraceId(String traceId, UUID eventId) {
        return traceId == null || traceId.isBlank() ? eventId.toString() : traceId;
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
    ) {}
}
