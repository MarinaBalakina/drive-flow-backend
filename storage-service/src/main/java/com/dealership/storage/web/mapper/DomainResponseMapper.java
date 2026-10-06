package com.dealership.storage.web.mapper;

import com.dealership.storage.domain.entity.assembly.AssemblyOrder;
import com.dealership.storage.domain.entity.car.Car;
import com.dealership.storage.domain.entity.car.CarDetails;
import com.dealership.storage.domain.entity.configurator.CarModel;
import com.dealership.storage.domain.entity.configurator.ComponentOption;
import com.dealership.storage.domain.entity.configurator.ComponentType;
import com.dealership.storage.domain.entity.configurator.SelectedConfiguration;
import com.dealership.storage.domain.entity.sparePart.SparePart;
import com.dealership.storage.web.dto.response.*;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class DomainResponseMapper {
    public AssemblyOrderResponse toResponse(AssemblyOrder order) {
        return new AssemblyOrderResponse(
                order.getId(),
                order.getSourceOrderId(),
                order.getOrderType(),
                order.getCarId(),
                order.getCarModelId(),
                order.getRequiredComponentOptionIds(),
                order.getWarehouseEmployeeId(),
                order.getOrderStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.isRemoved(),
                order.getTraceId()
        );
    }

    public CarResponse toResponse(Car car) {
        return new CarResponse(
                car.getId(),
                car.getPrice(),
                car.getBrand(),
                car.getModel(),
                car.getStatus(),
                car.isTestDriveEnabled(),
                toResponse(car.getDetails())
        );
    }

    public CarDetailsResponse toResponse(CarDetails details) {
        return new CarDetailsResponse(
                details.getBodyType(),
                details.getFuelType(),
                details.getEnginePower(),
                details.getEngineCapacity(),
                details.getTransmission(),
                details.getDriveType(),
                details.getColor()
        );
    }

    public ComponentOptionResponse toResponse(ComponentOption option) {
        return new ComponentOptionResponse(
                option.getId(),
                option.getType(),
                option.getName(),
                option.getPriceChange(),
                option.getCompatibleModels().stream().sorted().toList()
        );
    }

    public SelectedConfigurationResponse toResponse(SelectedConfiguration selectedConfiguration) {
        Map<ComponentType, ComponentOptionResponse> selected = new EnumMap<>(ComponentType.class);
        selectedConfiguration.getSelected().forEach((type, option) -> selected.put(type, toResponse(option)));
        return new SelectedConfigurationResponse(selected);
    }

    public CarModelResponse toResponse(CarModel carModel) {
        return new CarModelResponse(
                carModel.getId(),
                carModel.getModel(),
                carModel.getBasePrice(),
                mapBaseOptions(carModel.getBaseOptions()),
                mapAcceptableOptions(carModel.getAcceptableOptions())
        );
    }

    public SparePartResponse toResponse(SparePart sparePart) {
        return new SparePartResponse(
                sparePart.getId(),
                sparePart.getName(),
                sparePart.getPrice(),
                sparePart.getCompatibleModels(),
                sparePart.getComponentOptionId(),
                sparePart.getQuantity(),
                sparePart.getReservedQuantity(),
                sparePart.getAvailableQuantity()
        );
    }

    private Map<ComponentType, ComponentOptionResponse> mapBaseOptions(
            Map<ComponentType, ComponentOption> source
    ) {
        Map<ComponentType, ComponentOptionResponse> mapped = new EnumMap<>(ComponentType.class);
        source.forEach((type, option) -> mapped.put(type, toResponse(option)));
        return mapped;
    }

    private Map<ComponentType, List<ComponentOptionResponse>> mapAcceptableOptions(
            Map<ComponentType, List<ComponentOption>> source
    ) {
        Map<ComponentType, List<ComponentOptionResponse>> mapped = new EnumMap<>(ComponentType.class);
        source.forEach((type, options) -> mapped.put(
                type,
                options.stream().map(this::toResponse).toList()
        ));
        return mapped;
    }
}
