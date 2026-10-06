package com.dealership.storage.idempotency;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
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
    @Column(name = "eventId", nullable = false)
    private UUID eventId;

    @Column(name = "eventType")
    private String eventType;

    @Column(name = "orderId")
    private UUID orderId;

    @Column(name = "traceId")
    private String traceId;

    @Column(name = "processedAt")
    private Instant processedAt;

    public ProcessedMessage(UUID eventId, String eventType, UUID orderId, String traceId) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.orderId = orderId;
        this.traceId = traceId;
        this.processedAt = Instant.now();
    }
}
