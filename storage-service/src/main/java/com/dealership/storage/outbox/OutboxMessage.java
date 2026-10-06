package com.dealership.storage.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages")
@Getter
@NoArgsConstructor
public class OutboxMessage {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "exchangeName", nullable = false)
    private String exchangeName;

    @Column(name = "routingKey", nullable = false)
    private String routingKey;

    @Column(name = "eventType", nullable = false)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    @Column(name = "publishedAt")
    private Instant publishedAt;

    @Column(name = "published", nullable = false)
    private boolean published;

    public OutboxMessage(String exchangeName, String routingKey, String eventType, String payload){
        this.id = UUID.randomUUID();
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = Instant.now();
        this.published = false;
    }

    public void markPublished(){
        this.published = true;
        this.publishedAt = Instant.now();
    }

}
