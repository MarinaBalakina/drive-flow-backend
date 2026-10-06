package com.dealership.storage.web.controller;

import com.dealership.storage.application.usecase.WarehouseService;
import com.dealership.storage.domain.entity.car.Car;
import com.dealership.storage.domain.entity.car.CarDetails;
import com.dealership.storage.domain.entity.configurator.CarModel;
import com.dealership.storage.domain.entity.configurator.ComponentOption;
import com.dealership.storage.domain.entity.configurator.ComponentType;
import com.dealership.storage.domain.entity.sparePart.SparePart;
import com.dealership.storage.domain.exception.DomainValidationException;
import com.dealership.storage.web.dto.request.SaveCarModelRequest;
import com.dealership.storage.web.dto.request.SaveCarRequest;
import com.dealership.storage.web.dto.request.SaveComponentOptionRequest;
import com.dealership.storage.web.dto.request.SaveSparePartRequest;
import com.dealership.storage.web.dto.response.CarModelResponse;
import com.dealership.storage.web.dto.response.CarResponse;
import com.dealership.storage.web.dto.response.SparePartResponse;
import com.dealership.storage.web.mapper.DomainResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/warehouse")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
public class WarehouseController {
    private final WarehouseService warehouseService;
    private final DomainResponseMapper responseMapper;

    @PostMapping("/cars")
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse createCar(@Valid @RequestBody SaveCarRequest request) {
        UUID id = request.id() != null ? request.id() : UUID.randomUUID();
        Car created = warehouseService.addCar(toDomain(request, id));
        return responseMapper.toResponse(created);
    }

    @PutMapping("/cars/{carId}")
    public CarResponse updateCar(@PathVariable UUID carId, @RequestBody SaveCarRequest request) {
        Car updated = warehouseService.updateCar(toDomain(request, carId));
        return responseMapper.toResponse(updated);
    }

    @GetMapping("/cars")
    public List<CarResponse> listCars() {
        return warehouseService.listAllCars().stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @PostMapping("/models")
    @ResponseStatus(HttpStatus.CREATED)
    public CarModelResponse createCarModel(@Valid @RequestBody SaveCarModelRequest request) {
        UUID id = request.id() != null ? request.id() : UUID.randomUUID();
        CarModel created = warehouseService.addCarModel(toDomain(request, id));
        return responseMapper.toResponse(created);
    }

    @PutMapping("/models/{modelId}")
    public CarModelResponse updateCarModel(
            @PathVariable("modelId") UUID carModelId,
            @Valid @RequestBody SaveCarModelRequest request
    ) {
        CarModel updated = warehouseService.updateCarModel(toDomain(request, carModelId));
        return responseMapper.toResponse(updated);
    }

    @GetMapping("/models")
    public List<CarModelResponse> listCarModels(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) List<UUID> componentOptionIds
    ) {
        Set<UUID> optionIds = componentOptionIds == null
                ? Set.of()
                : new LinkedHashSet<>(componentOptionIds);
        return warehouseService.listBaseConfigurations(brand, optionIds).stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @GetMapping("/models/{modelId}")
    public CarModelResponse getCarModelById(@PathVariable UUID modelId) {
        return responseMapper.toResponse(warehouseService.getCarModelById(modelId));
    }

    @PostMapping("/spare-parts")
    @ResponseStatus(HttpStatus.CREATED)
    public SparePartResponse createSparePart(@Valid @RequestBody SaveSparePartRequest request) {
        UUID id = request.id() != null ? request.id() : UUID.randomUUID();
        SparePart created = warehouseService.addSparePart(toDomain(request, id));
        return responseMapper.toResponse(created);
    }

    @PutMapping("/spare-part/{partId}")
    public SparePartResponse updateSparePart(
            @PathVariable UUID partId,
            @Valid @RequestBody SaveSparePartRequest request
    ) {
        SparePart updated = warehouseService.updateSparePart(toDomain(request, partId));
        return responseMapper.toResponse(updated);
    }

    @GetMapping("/spare-part")
    public List<SparePartResponse> listSparePart() {
        return warehouseService.listAllParts().stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    private Car toDomain(SaveCarRequest request, UUID id) {
        CarDetails details = new CarDetails(
                request.details().bodyType(),
                request.details().fuelType(),
                request.details().enginePower(),
                request.details().engineCapacity(),
                request.details().transmission(),
                request.details().driveType(),
                request.details().color()
        );

        return new Car(
                id,
                request.price(),
                request.brand(),
                request.model(),
                details,
                request.status(),
                request.testDriveEnabled()
        );
    }

    private CarModel toDomain(SaveCarModelRequest request, UUID id) {
        Map<ComponentType, ComponentOption> baseOptions = request.baseOptions().entrySet().stream().collect(
                () -> new EnumMap<>(ComponentType.class),
                (map, e) -> map.put(e.getKey(), toDomain(e.getValue(), e.getKey())),
                Map::putAll
        );

        Map<ComponentType, List<ComponentOption>> acceptableOptions = request.acceptableOptions()
                .entrySet().stream().collect(
                        () -> new EnumMap<>(ComponentType.class),
                        (map, e) -> map.put(
                                e.getKey(),
                                e.getValue().stream()
                                        .map(optionRequest -> toDomain(optionRequest, e.getKey()))
                                        .toList()
                        ),
                        Map::putAll
                );

        return CarModel.of(
                id,
                request.model(),
                request.basePrice(),
                baseOptions,
                acceptableOptions
        );
    }

    private ComponentOption toDomain(SaveComponentOptionRequest request, ComponentType expectedType) {
        if (request.type() != expectedType) {
            throw new DomainValidationException(
                    "Component type mismatch: map key=" + expectedType + ", request.type=" + request.type()
            );
        }

        UUID id = request.id() != null ? request.id() : UUID.randomUUID();

        return new ComponentOption(
                id,
                request.type(),
                request.name(),
                request.priceChange(),
                new LinkedHashSet<>(request.compatibleModels())
        );
    }

    private SparePart toDomain(SaveSparePartRequest request, UUID id) {
        return new SparePart(
                id,
                request.name(),
                request.price(),
                new LinkedHashSet<>(request.compatibleModels()),
                request.componentOptionId(),
                request.quantity() == null ? 0 : request.quantity(),
                request.reservedQuantity() == null ? 0 : request.reservedQuantity()
        );
    }
}

