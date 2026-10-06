package com.dealership.order.application.port;

import com.dealership.order.domain.entity.order.InStockOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InStockOrderRepository {
    InStockOrder save(InStockOrder order);

    Optional<InStockOrder> findById(UUID id);

    List<InStockOrder> findAll();

    List<InStockOrder> findAllByClientId(UUID clientId);

    void deleteById(UUID orderId);
}
