package com.dealership.order.web.controller;

import com.dealership.order.application.usecase.TestDriveService;
import com.dealership.order.domain.entity.testDrive.TestDriveRequest;
import com.dealership.order.infrastructure.security.CurrentUserProvider;
import com.dealership.order.web.dto.request.CreateTestDriveRequest;
import com.dealership.order.web.dto.response.TestDriveRequestResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/test-drives")
@RequiredArgsConstructor
@Validated
public class TestDriveController {
    private final TestDriveService testDriveService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public TestDriveRequestResponse create(@Valid @RequestBody CreateTestDriveRequest request) {
        UUID clientId = currentUserProvider.getCurrentUserId();
        return toResponse(
                testDriveService.create(clientId, request.carId(), request.startAt())
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public List<TestDriveRequestResponse> listAll() {
        return testDriveService.listAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public TestDriveRequestResponse getById(@PathVariable UUID requestId) {
        return toResponse(testDriveService.getById(requestId));
    }

    @DeleteMapping("/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public void cancel(@PathVariable UUID requestId) {
        testDriveService.cancel(requestId);
    }

    private TestDriveRequestResponse toResponse(TestDriveRequest request) {
        return new TestDriveRequestResponse(
                request.getId(),
                request.getClientId(),
                request.getCarId(),
                request.getStartAt()
        );
    }
}
