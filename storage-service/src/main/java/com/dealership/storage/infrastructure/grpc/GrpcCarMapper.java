package com.dealership.storage.infrastructure.grpc;

import com.dealership.storage.domain.entity.car.Car;
import com.dealership.storage.domain.entity.car.CarDetails;
import org.springframework.stereotype.Component;

@Component
public class GrpcCarMapper {
    public com.dealership.grpc.car.Car toGrpcCar(Car car){
        return com.dealership.grpc.car.Car.newBuilder()
                .setId(car.getId().toString())
                .setPrice(car.getPrice().toString())
                .setBrand(car.getBrand())
                .setModel(car.getModel())
                .setStatus(car.getStatus().toString())
                .setTestDriveEnabled(car.isTestDriveEnabled())
                .setDetails(toGrpcCarDetails(car.getDetails()))
                .build();


    }

    private com.dealership.grpc.car.CarDetails toGrpcCarDetails(CarDetails carDetails){
        return com.dealership.grpc.car.CarDetails.newBuilder()
                .setBodyType(carDetails.getBodyType())
                .setFuelType(carDetails.getFuelType())
                .setEnginePower(carDetails.getEnginePower())
                .setEngineCapacity(carDetails.getEngineCapacity())
                .setTransmission(carDetails.getTransmission())
                .setDriveType(carDetails.getDriveType())
                .setColor(carDetails.getColor())
                .build();
    }
}
