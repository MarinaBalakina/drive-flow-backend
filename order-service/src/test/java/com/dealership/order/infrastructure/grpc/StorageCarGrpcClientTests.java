package com.dealership.order.infrastructure.grpc;

import com.dealership.grpc.car.CarInventoryServiceGrpc;
import com.dealership.grpc.car.GetAvailableCarByIdRequest;
import com.dealership.grpc.car.GetAvailableCarByIdResponse;
import com.dealership.grpc.car.GetAvailableCarsRequest;
import com.dealership.grpc.car.GetAvailableCarsResponse;
import com.dealership.order.domain.exception.EntityNotFoundException;
import com.dealership.order.domain.exception.StorageServiceUnavailableException;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageCarGrpcClientTests {
    private static final UUID CAR_ID = UUID.fromString("66666666-6666-6666-6666-666666666661");

    private Server server;
    private ManagedChannel channel;

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
    void carsMappedFromGrpc() throws Exception {
        StorageCarGrpcClient client = startClient(new CarInventoryServiceGrpc.CarInventoryServiceImplBase() {
            @Override
            public void getAvailableCars(
                    GetAvailableCarsRequest request,
                    StreamObserver<GetAvailableCarsResponse> responseObserver
            ) {
                responseObserver.onNext(GetAvailableCarsResponse.newBuilder()
                        .addCars(grpcCar(CAR_ID))
                        .build());
                responseObserver.onCompleted();
            }
        }, 5000);

        var cars = client.getAvailableCars();

        Assertions.assertEquals(1, cars.size());
        Assertions.assertEquals(CAR_ID, cars.getFirst().id());
        Assertions.assertEquals(new BigDecimal("18990000.00"), cars.getFirst().price());
        Assertions.assertEquals("PORSCHE", cars.getFirst().brand());
        Assertions.assertEquals("911 Carrera", cars.getFirst().model());
        Assertions.assertEquals("AVAILABLE", cars.getFirst().status());
        Assertions.assertEquals("COUPE", cars.getFirst().details().bodyType());
    }

    @Test
    void carByIdMapped() throws Exception {
        StorageCarGrpcClient client = startClient(new CarInventoryServiceGrpc.CarInventoryServiceImplBase() {
            @Override
            public void getAvailableCarById(
                    GetAvailableCarByIdRequest request,
                    StreamObserver<GetAvailableCarByIdResponse> responseObserver
            ) {
                responseObserver.onNext(GetAvailableCarByIdResponse.newBuilder()
                        .setCar(grpcCar(UUID.fromString(request.getId())))
                        .build());
                responseObserver.onCompleted();
            }
        }, 5000);

        var car = client.getAvailableCarById(CAR_ID);

        Assertions.assertEquals(CAR_ID, car.id());
        Assertions.assertEquals("PORSCHE", car.brand());
    }

    @Test
    void emptyListMapped() throws Exception {
        StorageCarGrpcClient client = startClient(new CarInventoryServiceGrpc.CarInventoryServiceImplBase() {
            @Override
            public void getAvailableCars(
                    GetAvailableCarsRequest request,
                    StreamObserver<GetAvailableCarsResponse> responseObserver
            ) {
                responseObserver.onNext(GetAvailableCarsResponse.newBuilder().build());
                responseObserver.onCompleted();
            }
        }, 5000);

        var cars = client.getAvailableCars();

        Assertions.assertEquals(0, cars.size());
    }

    @Test
    void timeoutMapped() throws Exception {
        StorageCarGrpcClient client = startClient(new CarInventoryServiceGrpc.CarInventoryServiceImplBase() {
            @Override
            public void getAvailableCars(
                    GetAvailableCarsRequest request,
                    StreamObserver<GetAvailableCarsResponse> responseObserver
            ) {
            }
        }, 50);

        assertThrows(StorageServiceUnavailableException.class, client::getAvailableCars);
    }

    @Test
    void unavailableMapped() {
        StorageCarGrpcClient client = clientForMissingServer();

        assertThrows(StorageServiceUnavailableException.class, client::getAvailableCars);
    }

    @Test
    void notFoundMapped() throws Exception {
        StorageCarGrpcClient client = startClient(new CarInventoryServiceGrpc.CarInventoryServiceImplBase() {
            @Override
            public void getAvailableCarById(
                    GetAvailableCarByIdRequest request,
                    StreamObserver<GetAvailableCarByIdResponse> responseObserver
            ) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Available car not found")
                        .asRuntimeException());
            }
        }, 5000);

        assertThrows(EntityNotFoundException.class, () -> client.getAvailableCarById(CAR_ID));
    }

    private StorageCarGrpcClient startClient(
            CarInventoryServiceGrpc.CarInventoryServiceImplBase service,
            long timeoutMs
    ) throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(service)
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        return client(channel, timeoutMs);
    }

    private StorageCarGrpcClient clientForMissingServer() {
        String serverName = InProcessServerBuilder.generateName();
        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        return client(channel, 5000);
    }

    private StorageCarGrpcClient client(ManagedChannel channel, long timeoutMs) {
        StorageCarGrpcClient client = new StorageCarGrpcClient(new GrpcAvailableCarMapper());
        ReflectionTestUtils.setField(
                client,
                "blockingStub",
                CarInventoryServiceGrpc.newBlockingStub(channel)
        );
        ReflectionTestUtils.setField(client, "timeoutMs", timeoutMs);
        return client;
    }

    private com.dealership.grpc.car.Car grpcCar(UUID id) {
        return com.dealership.grpc.car.Car.newBuilder()
                .setId(id.toString())
                .setPrice("18990000.00")
                .setBrand("PORSCHE")
                .setModel("911 Carrera")
                .setStatus("AVAILABLE")
                .setTestDriveEnabled(true)
                .setDetails(com.dealership.grpc.car.CarDetails.newBuilder()
                        .setBodyType("COUPE")
                        .setFuelType("GASOLINE")
                        .setEnginePower(480)
                        .setEngineCapacity(3.0)
                        .setTransmission("AUTOMATIC")
                        .setDriveType("AWD")
                        .setColor("SILVER")
                        .build())
                .build();
    }
}