package com.dealership.storage.infrastructure.config;

import com.dealership.storage.application.port.AssemblyOrderRepository;
import com.dealership.storage.application.port.CarModelRepository;
import com.dealership.storage.application.port.CarRepository;
import com.dealership.storage.application.port.SparePartRepository;
import com.dealership.storage.application.usecase.AssemblyOrderService;
import com.dealership.storage.application.usecase.CarCatalogService;
import com.dealership.storage.application.usecase.ConfiguratorService;
import com.dealership.storage.application.usecase.WarehouseService;
import com.dealership.storage.domain.service.CarConfigurator;
import com.dealership.storage.messaging.OrderApprovedPublisher;
import com.dealership.storage.messaging.OrderRejectedPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    @Bean
    CarConfigurator carConfigurator() {
        return new CarConfigurator();
    }

    @Bean
    CarCatalogService carCatalogService(CarRepository carRepository) {
        return new CarCatalogService(carRepository);
    }

    @Bean
    ConfiguratorService configuratorService(
            CarModelRepository modelRepository,
            CarConfigurator carConfigurator
    ) {
        return new ConfiguratorService(modelRepository, carConfigurator);
    }

    @Bean
    WarehouseService warehouseService(
            CarRepository carRepository,
            SparePartRepository sparePartRepository,
            CarModelRepository carModelRepository
    ) {
        return new WarehouseService(carRepository, sparePartRepository, carModelRepository);
    }

    @Bean
    AssemblyOrderService assemblyOrderService(AssemblyOrderRepository orderRepository,
                                              CarRepository carRepository,
                                              CarModelRepository carModelRepository,
                                              SparePartRepository sparePartRepository,
                                              OrderApprovedPublisher orderApprovedPublisher,
                                              OrderRejectedPublisher orderRejectedPublisher) {
        return new AssemblyOrderService(
                orderRepository,
                carRepository,
                carModelRepository,
                sparePartRepository,
                orderApprovedPublisher,
                orderRejectedPublisher
        );
    }
}
