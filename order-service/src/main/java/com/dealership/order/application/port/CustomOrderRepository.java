package com.dealership.order.application.port;

import com.dealership.order.domain.entity.order.CustomOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomOrderRepository {
    CustomOrder save(CustomOrder order);

    Optional<CustomOrder> findById(UUID id);

    List<CustomOrder> findAll();

    void deleteById(UUID orderId);

    List<CustomOrder> findAllByClientId(UUID clientId);
}
