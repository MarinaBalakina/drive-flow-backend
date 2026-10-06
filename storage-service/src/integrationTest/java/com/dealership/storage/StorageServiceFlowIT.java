package com.dealership.storage;

import com.dealership.storage.application.port.AssemblyOrderRepository;
import com.dealership.storage.domain.entity.assembly.AssemblyOrder;
import com.dealership.storage.domain.entity.assembly.AssemblySourceOrderType;
import com.dealership.storage.idempotency.ProcessedMessageRepository;
import com.dealership.storage.messaging.OrderSentForApprovalHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class StorageServiceFlowIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("storage_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Autowired
    private OrderSentForApprovalHandler orderSentForApprovalHandler;

    @Autowired
    private AssemblyOrderRepository assemblyOrderRepository;

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "http://localhost/realms/test");
        registry.add("grpc.server.port", () -> 0);
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> "http://localhost/not-used");
    }

    @Test
    void sameSourceOrderCreatesOneAssemblyOrder() {
        UUID firstEventId = UUID.randomUUID();
        UUID secondEventId = UUID.randomUUID();
        UUID sourceOrderId = UUID.randomUUID();
        UUID carModelId = UUID.fromString("22222222-2222-2222-2222-222222222221");
        UUID employeeId = UUID.randomUUID();

        orderSentForApprovalHandler.process(
                firstEventId,
                sourceOrderId,
                AssemblySourceOrderType.CUSTOM_ORDER,
                null,
                carModelId,
                options(),
                employeeId,
                "trace"
        );
        orderSentForApprovalHandler.process(
                secondEventId,
                sourceOrderId,
                AssemblySourceOrderType.CUSTOM_ORDER,
                null,
                carModelId,
                options(),
                employeeId,
                "trace"
        );

        List<AssemblyOrder> orders = assemblyOrderRepository.findAllBySourceOrderId(sourceOrderId);
        assertEquals(1, orders.size());
        assertTrue(processedMessageRepository.existsById(firstEventId));
        assertTrue(processedMessageRepository.existsById(secondEventId));
    }

    private Set<UUID> options() {
        return Set.of(
                UUID.fromString("44444444-4444-4444-4444-444444444441"),
                UUID.fromString("44444444-4444-4444-4444-444444444443"),
                UUID.fromString("44444444-4444-4444-4444-444444444445"),
                UUID.fromString("44444444-4444-4444-4444-444444444447")
        );
    }
}
