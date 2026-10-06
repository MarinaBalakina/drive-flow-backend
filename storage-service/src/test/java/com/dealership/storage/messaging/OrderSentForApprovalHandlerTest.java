package com.dealership.storage.messaging;

import com.dealership.storage.application.port.AssemblyOrderRepository;
import com.dealership.storage.application.usecase.AssemblyOrderService;
import com.dealership.storage.domain.entity.assembly.AssemblySourceOrderType;
import com.dealership.storage.idempotency.ProcessedMessage;
import com.dealership.storage.idempotency.ProcessedMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderSentForApprovalHandlerTest {
    @Mock
    private AssemblyOrderService assemblyOrderService;

    @Mock
    private AssemblyOrderRepository assemblyOrderRepository;

    @Mock
    private ProcessedMessageRepository processedMessageRepository;

    @Mock
    private OrderRejectedPublisher orderRejectedPublisher;

    @Test
    void processedMessageIsIgnored() {
        UUID eventId = UUID.randomUUID();
        OrderSentForApprovalHandler handler = handler();

        when(processedMessageRepository.existsById(eventId)).thenReturn(true);

        handler.process(
                eventId,
                UUID.randomUUID(),
                AssemblySourceOrderType.CUSTOM_ORDER,
                null,
                UUID.randomUUID(),
                Set.of(UUID.randomUUID()),
                UUID.randomUUID(),
                "trace"
        );

        verify(assemblyOrderService, never()).create(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void newMessageCreatesAssemblyOrder() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID carModelId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        Set<UUID> options = Set.of(UUID.randomUUID());
        OrderSentForApprovalHandler handler = handler();

        when(processedMessageRepository.existsById(eventId)).thenReturn(false);
        when(assemblyOrderRepository.existsBySourceOrderIdAndOrderType(orderId, AssemblySourceOrderType.CUSTOM_ORDER))
                .thenReturn(false);

        handler.process(
                eventId,
                orderId,
                AssemblySourceOrderType.CUSTOM_ORDER,
                null,
                carModelId,
                options,
                employeeId,
                "trace"
        );

        verify(assemblyOrderService).create(
                orderId,
                AssemblySourceOrderType.CUSTOM_ORDER,
                null,
                carModelId,
                options,
                employeeId,
                "trace"
        );
        verify(processedMessageRepository).save(any(ProcessedMessage.class));
    }

    @Test
    void duplicateSourceOrderIsOnlyMarkedProcessed() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderSentForApprovalHandler handler = handler();

        when(processedMessageRepository.existsById(eventId)).thenReturn(false);
        when(assemblyOrderRepository.existsBySourceOrderIdAndOrderType(orderId, AssemblySourceOrderType.IN_STOCK_ORDER))
                .thenReturn(true);

        handler.process(
                eventId,
                orderId,
                AssemblySourceOrderType.IN_STOCK_ORDER,
                UUID.randomUUID(),
                null,
                null,
                UUID.randomUUID(),
                "trace"
        );

        verify(assemblyOrderService, never()).create(any(), any(), any(), any(), any(), any(), any());
        verify(processedMessageRepository).save(any(ProcessedMessage.class));
    }

    @Test
    void rejectedMessageIsSavedToOutbox() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderSentForApprovalHandler handler = handler();

        when(processedMessageRepository.existsById(eventId)).thenReturn(false);

        handler.reject(eventId, orderId, AssemblySourceOrderType.CUSTOM_ORDER, "trace");

        verify(orderRejectedPublisher).publish(orderId, AssemblySourceOrderType.CUSTOM_ORDER, "trace");
        verify(processedMessageRepository).save(any(ProcessedMessage.class));
    }

    private OrderSentForApprovalHandler handler() {
        return new OrderSentForApprovalHandler(
                assemblyOrderService,
                assemblyOrderRepository,
                processedMessageRepository,
                orderRejectedPublisher
        );
    }
}
