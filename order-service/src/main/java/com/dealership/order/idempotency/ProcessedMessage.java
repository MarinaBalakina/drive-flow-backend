package com.dealership.order.idempotency;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_messages")
@Getter
@NoArgsConstructor
public class ProcessedMessage {
    @Id
    private UUID eventId;

    private String eventType;
    private UUID orderId;
    private String traceId;
    private Instant processedAt;

    public ProcessedMessage (UUID eventId, String eventType, UUID orderId, String traceId){
        this.eventId = eventId;
        this.eventType = eventType;
        this.orderId = orderId;
        this.traceId = traceId;
        processedAt = Instant.now();
    }
}
