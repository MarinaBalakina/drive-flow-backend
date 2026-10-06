package com.dealership.order.infrastructure.config;

import com.dealership.order.application.port.*;
import com.dealership.order.application.usecase.AvailableCarCatalogService;
import com.dealership.order.application.usecase.CustomOrderService;
import com.dealership.order.application.usecase.InStockOrderService;
import com.dealership.order.application.usecase.TestDriveService;
import com.dealership.order.infrastructure.security.CurrentUserProvider;
import com.dealership.order.messaging.OrderSentForApprovalPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    @Bean
    CustomOrderService customOrderService(
            CustomOrderRepository orderRepository,
            CurrentUserProvider currentUserProvider,
            OrderSentForApprovalPublisher orderSentForApprovalPublisher,
            ConfigurationPricingPort configurationPricingPort
    ) {
        return new CustomOrderService(orderRepository, currentUserProvider, orderSentForApprovalPublisher,
                configurationPricingPort);
    }

    @Bean
    InStockOrderService inStockOrderService(
            InStockOrderRepository orderRepository,
            CurrentUserProvider currentUserProvider,
            OrderSentForApprovalPublisher orderSentForApprovalPublisher
    ) {
        return new InStockOrderService(orderRepository, currentUserProvider, orderSentForApprovalPublisher);
    }

    @Bean
    TestDriveService testDriveService(
            TestDriveRequestRepository requestRepository,
            CurrentUserProvider currentUserProvider
    ) {
        return new TestDriveService(requestRepository, currentUserProvider);
    }

    @Bean
    AvailableCarCatalogService availableCarCatalogService(
            AvailableCarCatalogPort availableCarCatalogPort
    ) {
        return new AvailableCarCatalogService(availableCarCatalogPort);
    }
}
