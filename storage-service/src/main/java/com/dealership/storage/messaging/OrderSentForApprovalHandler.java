package com.dealership.storage.messaging;

import com.dealership.storage.application.port.AssemblyOrderRepository;
import com.dealership.storage.application.usecase.AssemblyOrderService;
import com.dealership.storage.domain.entity.assembly.AssemblySourceOrderType;
import com.dealership.storage.idempotency.ProcessedMessage;
import com.dealership.storage.idempotency.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderSentForApprovalHandler {
    private final AssemblyOrderService assemblyOrderService;
    private final AssemblyOrderRepository assemblyOrderRepository;
    private final ProcessedMessageRepository processedMessageRepository;
    private final OrderRejectedPublisher orderRejectedPublisher;

    @Transactional
    public void process(UUID eventId, UUID orderId, AssemblySourceOrderType orderType,
                        UUID carId, UUID carModelId, Set<UUID> requiredComponentOptionIds,
                        UUID warehouseEmployeeId, String traceId) {
        if (processedMessageRepository.existsById(eventId)) {
            return;
        }

        if (!assemblyOrderRepository.existsBySourceOrderIdAndOrderType(orderId, orderType)) {
            assemblyOrderService.create(
                    orderId,
                    orderType,
                    carId,
                    carModelId,
                    requiredComponentOptionIds,
                    warehouseEmployeeId,
                    traceId
            );
        }

        processedMessageRepository.save(
                new ProcessedMessage(eventId, "OrderSentForApproval", orderId, traceId)
        );
    }

    @Transactional
    public void reject(UUID eventId, UUID orderId, AssemblySourceOrderType orderType, String traceId) {
        if (processedMessageRepository.existsById(eventId)) {
            return;
        }

        orderRejectedPublisher.publish(orderId, orderType, traceId);
        processedMessageRepository.save(
                new ProcessedMessage(eventId, "OrderSentForApproval", orderId, traceId)
        );
    }
}
