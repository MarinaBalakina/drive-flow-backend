package com.dealership.storage;

import com.rabbitmq.client.ConnectionFactory;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class StorageServiceInfrastructureIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("storage_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Test
    void liquibaseCreatesAssemblyOrdersAndStockTables() throws Exception {
        try (var connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        )) {
            var database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            try (var liquibase = new Liquibase(
                    "db/changelog/changelog-master.yaml",
                    new ClassLoaderResourceAccessor(),
                    database
            )) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        }

        try (var connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        )) {
            try (var rs = connection.createStatement().executeQuery("""
                    select count(*)
                    from "SpareParts"
                    where "ComponentOptionId" is not null
                    """)) {
                rs.next();
                assertTrue(rs.getInt(1) > 0);
            }
        }
    }

    @Test
    void rabbitMqAcceptsStorageServiceTopology() throws Exception {
        var factory = new ConnectionFactory();
        factory.setHost(rabbit.getHost());
        factory.setPort(rabbit.getAmqpPort());
        factory.setUsername(rabbit.getAdminUsername());
        factory.setPassword(rabbit.getAdminPassword());

        try (var connection = factory.newConnection();
             var channel = connection.createChannel()) {
            channel.exchangeDeclare("dealership-orders", "topic", true);
            channel.queueDeclare("storage-service.order-sent-for-approval", true, false, false, null);
            channel.queueBind(
                    "storage-service.order-sent-for-approval",
                    "dealership-orders",
                    "order.sent_for_approval"
            );
        }
    }
}
