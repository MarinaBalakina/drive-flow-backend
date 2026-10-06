package com.dealership.order.application.usecase;

import com.dealership.order.application.port.ConfigurationPricingPort;
import com.dealership.order.application.port.CustomOrderRepository;
import com.dealership.order.domain.entity.order.CustomOrder;
import com.dealership.order.domain.entity.order.CustomOrderStatus;
import com.dealership.order.infrastructure.security.CurrentUserProvider;
import com.dealership.order.messaging.OrderSentForApprovalPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOrderServiceTest {
    @Mock
    private CustomOrderRepository orderRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private OrderSentForApprovalPublisher orderSentForApprovalPublisher;

    @Mock
    private ConfigurationPricingPort configurationPricingPort;

    @Test
    void customOrderPriceIsCalculatedByStorage() {
        UUID clientId = UUID.randomUUID();
        UUID carModelId = UUID.randomUUID();
        Map<String, UUID> options = options();
        BigDecimal calculatedPrice = new BigDecimal("19000000.00");
        CustomOrderService service = service();

        when(configurationPricingPort.calculateTotalPrice("Porsche 911 Carrera 4 GTS", options))
                .thenReturn(calculatedPrice);
        when(orderRepository.save(any(CustomOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomOrder order = service.create(clientId, carModelId, "Porsche 911 Carrera 4 GTS", options);

        assertEquals(carModelId, order.getCarModelId());
        assertEquals(calculatedPrice, order.getTotalPrice());
        verify(configurationPricingPort).calculateTotalPrice("Porsche 911 Carrera 4 GTS", options);
    }

    @Test
    void customOrderCanWaitForPaymentAfterCreation() {
        CustomOrder order = CustomOrder.create(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                "Porsche 911 Carrera 4 GTS",
                options(),
                new BigDecimal("19000000.00")
        );

        order.markWaitingForPayment();

        assertEquals(CustomOrderStatus.WAITING_FOR_PAYMENT, order.getOrderStatus());
    }

    @Test
    void customOrderPaymentCreatesApprovalEvent() {
        UUID orderId = UUID.randomUUID();
        CustomOrder order = CustomOrder.create(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                "Porsche 911 Carrera 4 GTS",
                options(),
                new BigDecimal("19000000.00")
        );
        order.markWaitingForPayment();
        CustomOrder savedOrder = new CustomOrder(
                orderId,
                order.getClientId(),
                order.getCarModelId(),
                order.getFullModel(),
                order.getSelectedOptionsId(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                CustomOrderStatus.WAITING_FOR_PAYMENT
        );
        CustomOrderService service = service();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
        when(orderRepository.save(any(CustomOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.pay(orderId);

        assertEquals(CustomOrderStatus.PAID, savedOrder.getOrderStatus());
        verify(orderSentForApprovalPublisher).publish(any(CustomOrder.class), isNull());
    }

    private CustomOrderService service() {
        return new CustomOrderService(
                orderRepository,
                currentUserProvider,
                orderSentForApprovalPublisher,
                configurationPricingPort
        );
    }

    private Map<String, UUID> options() {
        Map<String, UUID> options = new LinkedHashMap<>();
        options.put("WHEELS", UUID.randomUUID());
        options.put("TRANSMISSION", UUID.randomUUID());
        options.put("STEERING_WHEEL", UUID.randomUUID());
        options.put("INTERIOR", UUID.randomUUID());
        return options;
    }
}
