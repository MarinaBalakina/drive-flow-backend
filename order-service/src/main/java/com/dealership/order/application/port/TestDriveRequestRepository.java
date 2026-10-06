package com.dealership.order.application.port;

import com.dealership.order.domain.entity.testDrive.TestDriveRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestDriveRequestRepository {
    TestDriveRequest save(TestDriveRequest request);

    Optional<TestDriveRequest> findById(UUID id);

    List<TestDriveRequest> findAll();

    List<TestDriveRequest> findAllByClientId(UUID clientId);

    void deleteById(UUID requestId);
}
