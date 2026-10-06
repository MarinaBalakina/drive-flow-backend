package com.dealership.order.infrastructure.db.adapter;

import com.dealership.order.application.port.TestDriveRequestRepository;
import com.dealership.order.domain.entity.testDrive.TestDriveRequest;
import com.dealership.order.domain.exception.DomainValidationException;
import com.dealership.order.infrastructure.db.entity.TestDriveRequestJpaEntity;
import com.dealership.order.infrastructure.db.repository.TestDriveRequestJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class TestDriveRequestRepositoryJpaAdapter implements TestDriveRequestRepository {
    private final TestDriveRequestJpaRepository jpa;

    @Override
    public TestDriveRequest save(TestDriveRequest request) {
        if (request == null) {
            throw new DomainValidationException("Test drive request must be not null");
        }

        TestDriveRequestJpaEntity entity = jpa.findById(request.getId()).orElseGet(TestDriveRequestJpaEntity::new);
        entity.setId(request.getId());
        entity.setCustomerId(request.getClientId());
        entity.setCarId(request.getCarId());
        entity.setStartAt(request.getStartAt());

        TestDriveRequestJpaEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TestDriveRequest> findById(UUID id) {
        if (id == null) {
            throw new DomainValidationException("Test drive request id must be not null");
        }

        return jpa.findByIdAndRemovedFalse(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestDriveRequest> findAll() {
        return jpa.findAllByRemovedFalse().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestDriveRequest> findAllByClientId(UUID clientId) {
        if (clientId == null) {
            throw new DomainValidationException("Client id must be not null");
        }

        return jpa.findAllByCustomerIdAndRemovedFalse(clientId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID requestId) {
        if (requestId == null) {
            throw new DomainValidationException("Test drive request id must be not null");
        }

        jpa.findByIdAndRemovedFalse(requestId).ifPresent(entity -> {
            entity.setRemoved(true);
            jpa.save(entity);
        });
    }

    private TestDriveRequest toDomain(TestDriveRequestJpaEntity entity) {
        if (entity.getCustomerId() == null || entity.getCarId() == null) {
            throw new DomainValidationException("Broken test-drive request links in DB: " + entity.getId());
        }

        return new TestDriveRequest(
                entity.getId(),
                entity.getCustomerId(),
                entity.getCarId(),
                entity.getStartAt()
        );
    }
}
