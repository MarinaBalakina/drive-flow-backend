package com.dealership.storage.messaging;

import com.dealership.storage.domain.entity.assembly.AssemblyOrder;
import com.dealership.storage.infrastructure.config.RabbitConfig;
import com.dealership.storage.outbox.OutboxMessage;
import com.dealership.storage.outbox.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderApprovedPublisher {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void publish(AssemblyOrder order, String traceId) {
        OrderApprovedEvent event = new OrderApprovedEvent(
                UUID.randomUUID(),
                order.getSourceOrderId(),
                order.getOrderType().name(),
                normalizeTraceId(traceId)
        );

        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxRepository.save(new OutboxMessage(
                    RabbitConfig.EXCHANGE,
                    RabbitConfig.ORDER_APPROVED_ROUTING_KEY,
                    "OrderApproved",
                    payload
            ));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize OrderApproved event", e);
        }
    }

    private String normalizeTraceId(String traceId) {
        return traceId == null || traceId.isBlank() ? UUID.randomUUID().toString() : traceId;
    }

    private record OrderApprovedEvent(
            UUID eventId,
            UUID orderId,
            String orderType,
            String traceId
    ) {
    }
}
