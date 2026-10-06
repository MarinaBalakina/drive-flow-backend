package com.dealership.storage.infrastructure.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@Getter @Setter
public abstract class BaseJpaEntity {
    @Id
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "UpdatedAt", nullable = false)
    private Instant updatedAt;

    @Column(name = "Removed", nullable = false)
    private boolean removed;

    @PrePersist
    void onCreate(){
        Instant now = Instant.now();
        if (id == null) id = UUID.randomUUID();
        createdAt = now;
        updatedAt = now;
        removed = false;
    }

    @PreUpdate
    void onUpdate(){
        updatedAt = Instant.now();
    }
}
