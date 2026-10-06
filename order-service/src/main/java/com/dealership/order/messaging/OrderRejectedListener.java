package com.dealership.order.messaging;

import com.dealership.order.application.usecase.CustomOrderService;
import com.dealership.order.application.usecase.InStockOrderService;
import com.dealership.order.idempotency.ProcessedMessage;
import com.dealership.order.idempotency.ProcessedMessageRepository;
import com.dealership.order.infrastructure.config.RabbitConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderRejectedListener {
    private final CustomOrderService customOrderService;
    private final InStockOrderService inStockOrderService;
    private final ObjectMapper objectMapper;
    private final ProcessedMessageRepository processedMessageRepository;

    @RabbitListener(queues = RabbitConfig.ORDER_REJECTED_QUEUE)
    @Transactional
    public void handle(String payload){
        try{
            OrderRejectedEvent event = objectMapper.readValue(payload, OrderRejectedEvent.class);
            MDC.put("traceId", event.traceId());

            if (processedMessageRepository.existsById(event.eventId()))
                return;

            switch (normalizeOrderType(event.orderType())) {
                case "CUSTOM_ORDER" -> customOrderService.rejectAfterStorageCheck(event.orderId());
                case "IN_STOCK_ORDER" -> inStockOrderService.rejectAfterStorageCheck(event.orderId());
                default -> throw new IllegalArgumentException("Unknown order type: " + event.orderType());
            }

            processedMessageRepository.save(
                    new ProcessedMessage(event.eventId(), "OrderRejected", event.orderId(), event.traceId()));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot handle OrderRejected event", e);
        } finally {
            MDC.remove("traceId");
        }
    }

    private String normalizeOrderType(String raw) {
        return raw == null ? "" : raw.trim().replace("-", "_").toUpperCase();
    }

    private record OrderRejectedEvent(
            UUID eventId,
            UUID orderId,
            String orderType,
            String traceId
    ){}
}
