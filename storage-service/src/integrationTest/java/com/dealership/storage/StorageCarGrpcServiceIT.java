package com.dealership.storage;

import com.dealership.grpc.car.CarInventoryServiceGrpc;
import com.dealership.grpc.car.GetAvailableCarByIdRequest;
import com.dealership.grpc.car.GetAvailableCarsRequest;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
class StorageCarGrpcServiceIT {

    private static final String GRPC_SERVER_NAME = "storage-grpc-it";
    private static final UUID AVAILABLE_CAR_ID =
            UUID.fromString("66666666-6666-6666-6666-666666666661");

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("storage_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static final RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    private ManagedChannel channel;
    private CarInventoryServiceGrpc.CarInventoryServiceBlockingStub stub;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> "http://localhost/realms/test");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> "http://localhost/not-used/jwks");

        registry.add("grpc.server.in-process-name", () -> GRPC_SERVER_NAME);
        registry.add("grpc.server.port", () -> -1);
    }

    @BeforeEach
    void setUp() {
        channel = InProcessChannelBuilder.forName(GRPC_SERVER_NAME)
                .directExecutor()
                .build();

        stub = CarInventoryServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdownNow();
        }
    }

    @Test
    void getAvailableCarsReturnsOnlyAvailableCarsFromDatabase() {
        var response = stub.getAvailableCars(GetAvailableCarsRequest.newBuilder().build());

        assertThat(response.getCarsList()).isNotEmpty();
        assertThat(response.getCarsList())
                .allMatch(car -> "AVAILABLE".equals(car.getStatus()));

        assertThat(response.getCarsList())
                .extracting(car -> car.getId())
                .contains(AVAILABLE_CAR_ID.toString());
    }

    @Test
    void getAvailableCarByIdReturnsCarFromDatabase() {
        var response = stub.getAvailableCarById(GetAvailableCarByIdRequest.newBuilder()
                .setId(AVAILABLE_CAR_ID.toString())
                .build());

        assertEquals(AVAILABLE_CAR_ID.toString(), response.getCar().getId());
        assertEquals("PORSCHE", response.getCar().getBrand());
        assertEquals("AVAILABLE", response.getCar().getStatus());
    }

    @Test
    void getUnknownCarByIdReturnsNotFound() {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () ->
                stub.getAvailableCarById(GetAvailableCarByIdRequest.newBuilder()
                        .setId(UUID.randomUUID().toString())
                        .build()));

        assertEquals(Status.Code.NOT_FOUND, exception.getStatus().getCode());
    }
}