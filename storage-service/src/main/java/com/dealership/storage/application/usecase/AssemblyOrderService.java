package com.dealership.storage.application.usecase;

import com.dealership.storage.application.port.AssemblyOrderRepository;
import com.dealership.storage.application.port.CarModelRepository;
import com.dealership.storage.application.port.CarRepository;
import com.dealership.storage.application.port.SparePartRepository;
import com.dealership.storage.domain.entity.assembly.AssemblyOrder;
import com.dealership.storage.domain.entity.assembly.AssemblyOrderStatus;
import com.dealership.storage.domain.entity.assembly.AssemblySourceOrderType;
import com.dealership.storage.domain.entity.car.Car;
import com.dealership.storage.domain.entity.configurator.CarModel;
import com.dealership.storage.domain.entity.sparePart.SparePart;
import com.dealership.storage.domain.exception.DomainValidationException;
import com.dealership.storage.domain.exception.EntityNotFoundException;
import com.dealership.storage.messaging.OrderApprovedPublisher;
import com.dealership.storage.messaging.OrderRejectedPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import lombok.NonNull;
import org.slf4j.MDC;


import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional
public class AssemblyOrderService {
    private final @NonNull AssemblyOrderRepository orderRepository;
    private final @NonNull CarRepository carRepository;
    private final @NonNull CarModelRepository carModelRepository;
    private final @NonNull SparePartRepository sparePartRepository;
    private final @NonNull OrderApprovedPublisher orderApprovedPublisher;
    private final @NonNull OrderRejectedPublisher orderRejectedPublisher;

    public AssemblyOrder create(UUID sourceOrderId, AssemblySourceOrderType orderType,
                                UUID carId, UUID carModelId, Set<UUID> requiredComponentOptionIds,
                                UUID warehouseEmployeeId, String traceId) {
        AssemblyOrder order = AssemblyOrder.create(sourceOrderId, orderType, carId, carModelId,
                requiredComponentOptionIds, warehouseEmployeeId, traceId);

        switch (order.getOrderType()){
            case IN_STOCK_ORDER -> prepareInStockOrder(order.getCarId());
            case CUSTOM_ORDER -> prepareCustomOrder(order.getCarModelId(), order.getRequiredComponentOptionIds());
        }

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public AssemblyOrder getOrderById(UUID orderId) {
        if (orderId == null)
            throw new DomainValidationException("Assembly order id must be not null");

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Assembly order not found"));

    }

    @Transactional(readOnly = true)
    public List<AssemblyOrder> listAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AssemblyOrder> findBySourceOrderId(UUID sourceOrderId) {
        if (sourceOrderId == null)
            throw new DomainValidationException("Assembly source order id must be not null");

        return orderRepository.findAllBySourceOrderId(sourceOrderId);
    }

    @Transactional(readOnly = true)
    public List<AssemblyOrder> findByAssemblyOrderStatus(AssemblyOrderStatus status) {
        if (status == null)
            throw new DomainValidationException("Assembly order status must be not null");

        return orderRepository.findAllByAssemblyOrderStatus(status);
    }

    public AssemblyOrder markAssembled(UUID orderId) {
        AssemblyOrder found = getOrderById(orderId);
        if (found.getOrderType() == AssemblySourceOrderType.CUSTOM_ORDER)
            consumeReservedComponents(found.getRequiredComponentOptionIds());

        found.markAssembled();
        AssemblyOrder saved = orderRepository.save(found);
        orderApprovedPublisher.publish(saved, resolveTraceId(saved));
        return saved;
    }

    public AssemblyOrder fail(UUID orderId) {
        AssemblyOrder found = getOrderById(orderId);
        found.fail();

        if (found.getOrderType() == AssemblySourceOrderType.IN_STOCK_ORDER)
            releaseReservedCar(found.getCarId());
        else
            releaseReservedComponents(found.getRequiredComponentOptionIds());

        AssemblyOrder saved = orderRepository.save(found);
        orderRejectedPublisher.publish(saved, resolveTraceId(saved));
        return saved;
    }

    public AssemblyOrder update(UUID orderId, UUID warehouseEmployeeId, AssemblyOrderStatus status) {
        AssemblyOrder found = getOrderById(orderId);

        if (warehouseEmployeeId != null) {
            found.assignWarehouseEmployee(warehouseEmployeeId);
            found = orderRepository.save(found);
        }

        if (status == null || status == found.getOrderStatus()) {
            return orderRepository.save(found);
        }

        return switch (status) {
            case ASSEMBLED -> markAssembled(orderId);
            case FAIL -> fail(orderId);
            case CREATED -> throw new DomainValidationException("Cannot return assembly order to CREATED status");
        };
    }

    public void deleteById(UUID orderId) {
        if (orderId == null)
            throw new DomainValidationException("Assembly order id must be not null");

        orderRepository.deleteById(orderId);
    }

    private void prepareInStockOrder(UUID carId) {
        if (carId == null)
            throw new DomainValidationException("Car id must be not null");

        Car car = carRepository.findById(carId).orElseThrow(() -> new EntityNotFoundException("Car isn't found"));

        car.markReserved();
        carRepository.save(car);
    }

    private void prepareCustomOrder(UUID carModelId, Set<UUID> requiredComponentOptionIds) {
        if (carModelId == null)
            throw new DomainValidationException("Car model id must be not null");

        if (requiredComponentOptionIds == null ||
                requiredComponentOptionIds.isEmpty() ||
                requiredComponentOptionIds.stream().anyMatch(Objects::isNull))
            throw new DomainValidationException("Component option id must be not null");

        CarModel carModel = carModelRepository.findById(carModelId).orElseThrow(
                () -> new EntityNotFoundException("Car model isn't found"));

        validateRequiredOptions(carModel, requiredComponentOptionIds);
        reserveRequiredComponents(requiredComponentOptionIds);

    }

    private void validateRequiredOptions(CarModel model, Set<UUID> requiredComponentOptionIds) {
        for (UUID optionId : requiredComponentOptionIds) {
            model.findOptionById(optionId);
        }
    }

    private void releaseReservedCar(UUID carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car isn't found"));

        car.releaseReservation();
        carRepository.save(car);
    }

    private void reserveRequiredComponents(Set<UUID> componentOptionIds) {
        for (UUID componentOptionId : componentOptionIds) {
            SparePart part = sparePartRepository.findByComponentOptionId(componentOptionId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Spare part stock isn't found for component option: " + componentOptionId));

            part.reserveOne();
            sparePartRepository.save(part);
        }
    }

    private void releaseReservedComponents(Set<UUID> componentOptionIds) {
        for (UUID componentOptionId : componentOptionIds) {
            SparePart part = sparePartRepository.findByComponentOptionId(componentOptionId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Spare part stock isn't found for component option: " + componentOptionId));

            part.releaseOneReservation();
            sparePartRepository.save(part);
        }
    }

    private void consumeReservedComponents(Set<UUID> componentOptionIds) {
        for (UUID componentOptionId : componentOptionIds) {
            SparePart part = sparePartRepository.findByComponentOptionId(componentOptionId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Spare part stock isn't found for component option: " + componentOptionId));

            part.consumeOneReserved();
            sparePartRepository.save(part);
        }
    }

    private String resolveTraceId(AssemblyOrder order) {
        String currentTraceId = MDC.get("traceId");
        return currentTraceId == null || currentTraceId.isBlank() ? order.getTraceId() : currentTraceId;
    }

}
