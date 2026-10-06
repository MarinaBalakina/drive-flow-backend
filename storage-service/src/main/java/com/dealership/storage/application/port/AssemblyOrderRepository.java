package com.dealership.storage.application.port;

import com.dealership.storage.domain.entity.assembly.AssemblyOrder;
import com.dealership.storage.domain.entity.assembly.AssemblyOrderStatus;
import com.dealership.storage.domain.entity.assembly.AssemblySourceOrderType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssemblyOrderRepository {
    AssemblyOrder save (AssemblyOrder order);

    Optional<AssemblyOrder> findById(UUID assemblyOrderId);

    List<AssemblyOrder> findAll();

    List<AssemblyOrder> findAllBySourceOrderId(UUID sourceOrderId);

    List<AssemblyOrder> findAllByAssemblyOrderStatus(AssemblyOrderStatus status);

    boolean existsBySourceOrderIdAndOrderType(UUID sourceOrderId, AssemblySourceOrderType orderType);

    void deleteById(UUID orderId);
}
