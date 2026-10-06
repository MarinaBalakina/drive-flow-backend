package com.dealership.order;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class OrderServiceInfrastructureIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("order_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Test
    void liquibaseCreatesOrderMessagingTables() throws Exception {
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
            try (var rs = connection.createStatement()
                    .executeQuery("select count(*) from outbox_messages")) {
                rs.next();
                assertEquals(0, rs.getInt(1));
            }
        }
    }

    @Test
    void rabbitMqAcceptsOrderServiceTopology() throws Exception {
        var factory = new ConnectionFactory();
        factory.setHost(rabbit.getHost());
        factory.setPort(rabbit.getAmqpPort());
        factory.setUsername(rabbit.getAdminUsername());
        factory.setPassword(rabbit.getAdminPassword());

        try (var connection = factory.newConnection();
             var channel = connection.createChannel()) {
            channel.exchangeDeclare("dealership-orders", "topic", true);
            channel.queueDeclare("order-service.order-approved", true, false, false, null);
            channel.queueBind("order-service.order-approved", "dealership-orders", "order.approved");
        }
    }
}
