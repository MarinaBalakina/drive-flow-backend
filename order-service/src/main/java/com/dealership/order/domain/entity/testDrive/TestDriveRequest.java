package com.dealership.order.domain.entity.testDrive;

import com.dealership.order.domain.exception.DomainValidationException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class TestDriveRequest {
    private final @NonNull UUID id;
    private final @NonNull UUID clientId;
    private final @NonNull UUID carId;
    private final @NonNull LocalDateTime startAt;

    public static TestDriveRequest create(UUID clientId, UUID carId, LocalDateTime startAt){
        if (clientId == null)
            throw new DomainValidationException("Client id must be not null");

        if (carId == null)
            throw new DomainValidationException("Car id must be not null");

        if (startAt == null)
            throw new DomainValidationException("Start time must be not null");

        if (!startAt.isAfter(LocalDateTime.now()))
            throw new DomainValidationException("Test drive start time must be in the future");

        return new TestDriveRequest(UUID.randomUUID(), clientId, carId, startAt);
    }


}

