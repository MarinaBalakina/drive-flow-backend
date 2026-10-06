package com.dealership.storage.messaging;

import com.dealership.storage.domain.entity.assembly.AssemblyOrder;
import com.dealership.storage.domain.entity.assembly.AssemblySourceOrderType;
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
public class OrderRejectedPublisher {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void publish(AssemblyOrder order, String traceId) {
        publish(order.getSourceOrderId(), order.getOrderType(), traceId);
    }

    public void publish(UUID orderId, AssemblySourceOrderType orderType, String traceId) {
        OrderRejectedEvent event = new OrderRejectedEvent(
                UUID.randomUUID(),
                orderId,
                orderType.name(),
                normalizeTraceId(traceId)
        );

        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxRepository.save(new OutboxMessage(
                    RabbitConfig.EXCHANGE,
                    RabbitConfig.ORDER_REJECTED_ROUTING_KEY,
                    "OrderRejected",
                    payload
            ));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize OrderRejected event", e);
        }
    }

    private String normalizeTraceId(String traceId) {
        return traceId == null || traceId.isBlank() ? UUID.randomUUID().toString() : traceId;
    }

    private record OrderRejectedEvent(
            UUID eventId,
            UUID orderId,
            String orderType,
            String traceId
    ) {
    }
}
