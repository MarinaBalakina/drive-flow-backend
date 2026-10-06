package com.dealership.order.infrastructure.grpc;

import com.dealership.order.application.readmodel.AvailableCar;
import com.dealership.order.application.readmodel.AvailableCarDetails;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class GrpcAvailableCarMapper {
    public AvailableCar toAvailableCar(com.dealership.grpc.car.Car grpcCar) {
        return new AvailableCar(
                UUID.fromString(grpcCar.getId()),
                new BigDecimal(grpcCar.getPrice()),
                grpcCar.getBrand(),
                grpcCar.getModel(),
                grpcCar.getStatus(),
                grpcCar.getTestDriveEnabled(),
                toAvailableCarDetails(grpcCar.getDetails())
        );
    }

    private AvailableCarDetails toAvailableCarDetails(com.dealership.grpc.car.CarDetails details) {
        return new AvailableCarDetails(
                details.getBodyType(),
                details.getFuelType(),
                details.getEnginePower(),
                details.getEngineCapacity(),
                details.getTransmission(),
                details.getDriveType(),
                details.getColor()
        );
    }
}
