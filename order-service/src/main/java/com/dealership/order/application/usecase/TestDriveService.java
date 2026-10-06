package com.dealership.order.application.usecase;

import com.dealership.order.application.port.TestDriveRequestRepository;
import com.dealership.order.domain.entity.testDrive.TestDriveRequest;
import com.dealership.order.domain.exception.DomainValidationException;
import com.dealership.order.domain.exception.EntityNotFoundException;
import com.dealership.order.infrastructure.security.CurrentUserProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class TestDriveService {
    private final @NonNull TestDriveRequestRepository requestRepository;
    private final @NonNull CurrentUserProvider currentUserProvider;

    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public TestDriveRequest create(UUID clientId, UUID carId, LocalDateTime startAt) {
        if (clientId == null)
            throw new DomainValidationException("Client id must be not null");

        if (carId == null)
            throw new DomainValidationException("Car id must be not null");


        TestDriveRequest request = TestDriveRequest.create(clientId, carId, startAt);

        requestRepository.save(request);

        return request;
    }

    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public List<TestDriveRequest> listAll() {
        if (currentUserProvider.hasRole("MANAGER") || currentUserProvider.hasRole("ADMIN")) {
            return requestRepository.findAll();
        }
        return requestRepository.findAllByClientId(currentUserProvider.getCurrentUserId());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('USER') and @orderSecurity.isTestDriveRequestOwner(#requestId))")
    public TestDriveRequest getById(UUID requestId) {
        if (requestId == null)
            throw new DomainValidationException("Test drive request id must be not null");

        return requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Test drive request not found: " + requestId));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('USER') and @orderSecurity.isTestDriveRequestOwner(#requestId))")
    public void cancel(UUID requestId) {
        getById(requestId);
        requestRepository.deleteById(requestId);
    }
}
