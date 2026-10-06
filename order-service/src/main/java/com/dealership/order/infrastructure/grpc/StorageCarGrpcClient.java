package com.dealership.order.infrastructure.grpc;

import com.dealership.grpc.car.*;
import com.dealership.order.application.port.AvailableCarCatalogPort;
import com.dealership.order.application.readmodel.AvailableCar;
import com.dealership.order.domain.exception.DomainValidationException;
import com.dealership.order.domain.exception.EntityNotFoundException;
import com.dealership.order.domain.exception.StorageServiceUnavailableException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageCarGrpcClient implements AvailableCarCatalogPort {
    private final GrpcAvailableCarMapper mapper;

    @GrpcClient("storage-car-service")
    private CarInventoryServiceGrpc.CarInventoryServiceBlockingStub blockingStub;

    @Value("${app.storage-grpc.timeout-ms:5000}")
    private long timeoutMs;

    public List<AvailableCar> getAvailableCars() {
        try {
            log.info("gRPC request started: getAvailableCars");

            GetAvailableCarsRequest request = GetAvailableCarsRequest.newBuilder().build();

            GetAvailableCarsResponse response = blockingStub
                    .withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                    .getAvailableCars(request);

            List<AvailableCar> cars = response.getCarsList().stream().map(mapper::toAvailableCar).toList();

            log.info("gRPC request completed: getAvailableCars, count={}", cars.size());

            return cars;
        } catch (StatusRuntimeException ex) {
            throw mapGrpcException("getAvailableCars", ex);
        }
    }

    public AvailableCar getAvailableCarById (UUID id) {
        try {
            log.info("gRPC request started: getAvailableCarById");

            GetAvailableCarByIdRequest request = GetAvailableCarByIdRequest.newBuilder()
                    .setId(id.toString()).build();

            GetAvailableCarByIdResponse response = blockingStub
                    .withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                    .getAvailableCarById(request);

            AvailableCar car = mapper.toAvailableCar(response.getCar());

            log.info("gRPC request completed: getAvailableCarById");

            return car;
        } catch (StatusRuntimeException ex) {
            throw mapGrpcException("getAvailableCarById", ex);
        }
    }

    private RuntimeException mapGrpcException(String operation, StatusRuntimeException ex) {
        Status.Code code = ex.getStatus().getCode();
        String description = ex.getStatus().getDescription();

        log.warn("gRPC request failed: {}, status={}, description={}", operation, code, description);

        return switch (code) {
            case UNAVAILABLE, DEADLINE_EXCEEDED ->
                    new StorageServiceUnavailableException("StorageService is unavailable", ex);
            case NOT_FOUND ->
                    new EntityNotFoundException(description != null ? description : "Available car not found");
            case INVALID_ARGUMENT ->
                    new DomainValidationException(description != null ? description : "Invalid gRPC request");
            default ->
                    new StorageServiceUnavailableException("StorageService request failed", ex);
        };
    }
}
