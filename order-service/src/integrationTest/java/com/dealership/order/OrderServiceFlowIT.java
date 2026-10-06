package com.dealership.order;

import com.dealership.order.application.port.ConfigurationPricingPort;
import com.dealership.order.application.usecase.CustomOrderService;
import com.dealership.order.domain.entity.order.CustomOrder;
import com.dealership.order.domain.entity.order.CustomOrderStatus;
import com.dealership.order.outbox.OutboxRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class OrderServiceFlowIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("order_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Autowired
    private CustomOrderService customOrderService;

    @Autowired
    private OutboxRepository outboxRepository;

    @MockitoBean
    private ConfigurationPricingPort configurationPricingPort;

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
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> "http://localhost/not-used");
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void customOrderPaymentCreatesOutboxMessage() {
        UUID clientId = UUID.randomUUID();
        UUID carModelId = UUID.fromString("22222222-2222-2222-2222-222222222221");
        Map<String, UUID> options = options();
        BigDecimal price = new BigDecimal("19000000.00");
        authenticate(clientId);

        when(configurationPricingPort.calculateTotalPrice("Porsche 911 Carrera 4 GTS", options)).thenReturn(price);

        CustomOrder created = customOrderService.create(
                clientId,
                carModelId,
                "Porsche 911 Carrera 4 GTS",
                options
        );
        customOrderService.markWaitingForPayment(created.getId());

        CustomOrder paid = customOrderService.pay(created.getId());

        var messages = outboxRepository.findTop50ByPublishedFalseOrderByCreatedAtAsc();
        assertEquals(CustomOrderStatus.PAID, paid.getOrderStatus());
        assertEquals(1, messages.size());
        assertTrue(messages.getFirst().getPayload().contains(created.getId().toString()));
        assertTrue(messages.getFirst().getPayload().contains(carModelId.toString()));
    }

    private void authenticate(UUID userId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("app_user_id", userId.toString())
                .claim("realm_access", Map.of("roles", List.of("ADMIN")))
                .subject("admin")
                .build();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                "admin"
        ));
    }

    private Map<String, UUID> options() {
        Map<String, UUID> options = new LinkedHashMap<>();
        options.put("WHEELS", UUID.fromString("44444444-4444-4444-4444-444444444441"));
        options.put("TRANSMISSION", UUID.fromString("44444444-4444-4444-4444-444444444443"));
        options.put("STEERING_WHEEL", UUID.fromString("44444444-4444-4444-4444-444444444445"));
        options.put("INTERIOR", UUID.fromString("44444444-4444-4444-4444-444444444447"));
        return options;
    }
}
