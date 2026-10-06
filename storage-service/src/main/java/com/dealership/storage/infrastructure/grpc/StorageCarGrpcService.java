package com.dealership.storage.infrastructure.grpc;

import com.dealership.grpc.car.CarInventoryServiceGrpc;
import com.dealership.grpc.car.GetAvailableCarsRequest;
import com.dealership.grpc.car.GetAvailableCarsResponse;
import com.dealership.storage.application.usecase.CarCatalogService;
import com.dealership.storage.domain.entity.car.Car;
import com.dealership.storage.domain.exception.EntityNotFoundException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import com.dealership.grpc.car.GetAvailableCarByIdResponse;
import com.dealership.grpc.car.GetAvailableCarByIdRequest;

import java.util.List;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class StorageCarGrpcService extends CarInventoryServiceGrpc.CarInventoryServiceImplBase{
    private final CarCatalogService carCatalogService;
    private final GrpcCarMapper grpcCarMapper;

    @Override
    public void getAvailableCars(GetAvailableCarsRequest request, StreamObserver<GetAvailableCarsResponse> responseObserver) {
        try {
            log.info("gRPC request received: getAvailableCars");

            List<Car> cars = carCatalogService.listAvailableCars(null);

            GetAvailableCarsResponse response = GetAvailableCarsResponse.newBuilder()
                    .addAllCars(cars.stream().map(grpcCarMapper::toGrpcCar).toList()).build();

            responseObserver.onNext(response);
            log.info("gRPC response sent: getAvailableCars, count={}", cars.size());
            responseObserver.onCompleted();
        } catch (RuntimeException ex) {
            log.error("gRPC request failed: getAvailableCars", ex);

            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to get available cars")
                    .withCause(ex)
                    .asRuntimeException()
            );
        }
    }

    @Override
    public void getAvailableCarById(GetAvailableCarByIdRequest request,
                                    StreamObserver<GetAvailableCarByIdResponse> responseObserver) {
        try {
            log.info("gRPC request received: getAvailableCarById");

            UUID carId = UUID.fromString(request.getId());

            Car car = carCatalogService.getAvailableCarById(carId);

            GetAvailableCarByIdResponse response = GetAvailableCarByIdResponse.newBuilder()
                    .setCar(grpcCarMapper.toGrpcCar(car)).build();

            responseObserver.onNext(response);
            log.info("gRPC response sent: getAvailableCarsById, id={}", car.getId());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid car id: " + request.getId())
                    .asRuntimeException());
        } catch (EntityNotFoundException ex) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
        } catch (RuntimeException ex) {
            log.error("gRPC request failed: getAvailableCarById, id={}", request.getId(), ex);

            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to get available cars by id")
                    .withCause(ex)
                    .asRuntimeException()
            );
        }
    }


}
