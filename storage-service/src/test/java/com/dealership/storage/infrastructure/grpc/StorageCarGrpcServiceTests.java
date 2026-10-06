package com.dealership.storage.infrastructure.grpc;

import com.dealership.grpc.car.CarInventoryServiceGrpc;
import com.dealership.grpc.car.GetAvailableCarByIdRequest;
import com.dealership.grpc.car.GetAvailableCarsRequest;
import com.dealership.storage.application.usecase.CarCatalogService;
import com.dealership.storage.domain.entity.car.CarDetails;
import com.dealership.storage.domain.entity.car.CarStatus;
import com.dealership.storage.domain.exception.EntityNotFoundException;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageCarGrpcServiceTests {
    private static final UUID CAR_ID = UUID.fromString("66666666-6666-6666-6666-666666666661");

    private CarCatalogService carCatalogService;
    private Server server;
    private ManagedChannel channel;
    private CarInventoryServiceGrpc.CarInventoryServiceBlockingStub stub;

    @BeforeEach
    void startServer() throws Exception {
        carCatalogService = mock(CarCatalogService.class);

        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new StorageCarGrpcService(carCatalogService, new GrpcCarMapper()))
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        stub = CarInventoryServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void stopServer() {
        if (channel != null) {
            channel.shutdownNow();
        }
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    void availableCarsReturned() {
        when(carCatalogService.listAvailableCars(isNull())).thenReturn(List.of(car(CAR_ID)));

        var response = stub.getAvailableCars(GetAvailableCarsRequest.newBuilder().build());

        assertEquals(1, response.getCarsCount());
        assertEquals(CAR_ID.toString(), response.getCars(0).getId());
        assertEquals("18990000.00", response.getCars(0).getPrice());
        assertEquals("PORSCHE", response.getCars(0).getBrand());
        assertEquals("911 Carrera", response.getCars(0).getModel());
        assertEquals("AVAILABLE", response.getCars(0).getStatus());
        assertEquals("COUPE", response.getCars(0).getDetails().getBodyType());
    }

    @Test
    void emptyListReturned() {
        when(carCatalogService.listAvailableCars(isNull())).thenReturn(List.of());

        var response = stub.getAvailableCars(GetAvailableCarsRequest.newBuilder().build());

        assertEquals(0, response.getCarsCount());
    }

    @Test
    void carByIdReturned() {
        when(carCatalogService.getAvailableCarById(CAR_ID)).thenReturn(car(CAR_ID));

        var response = stub.getAvailableCarById(GetAvailableCarByIdRequest.newBuilder()
                .setId(CAR_ID.toString())
                .build());

        assertEquals(CAR_ID.toString(), response.getCar().getId());
        assertEquals("PORSCHE", response.getCar().getBrand());
    }

    @Test
    void missingCarReturnsNotFound() {
        when(carCatalogService.getAvailableCarById(CAR_ID))
                .thenThrow(new EntityNotFoundException("Available car not found"));

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                stub.getAvailableCarById(GetAvailableCarByIdRequest.newBuilder()
                        .setId(CAR_ID.toString())
                        .build()));

        assertEquals(Status.Code.NOT_FOUND, ex.getStatus().getCode());
    }

    @Test
    void invalidIdReturnsBadRequest() {
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                stub.getAvailableCarById(GetAvailableCarByIdRequest.newBuilder()
                        .setId("bad-id")
                        .build()));

        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
    }

    private com.dealership.storage.domain.entity.car.Car car(UUID id) {
        return new com.dealership.storage.domain.entity.car.Car(
                id,
                new BigDecimal("18990000.00"),
                "PORSCHE",
                "911 Carrera",
                new CarDetails("COUPE", "GASOLINE", 480, 3.0, "AUTOMATIC", "AWD", "SILVER"),
                CarStatus.AVAILABLE,
                true
        );
    }
}