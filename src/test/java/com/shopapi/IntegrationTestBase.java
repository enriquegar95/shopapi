package com.shopapi;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

@SpringBootTest
@Transactional
public abstract class IntegrationTestBase {

    static final PostgreSQLContainer postgres;
    static final GenericContainer<?> redis;
    static final RabbitMQContainer rabbitmq;

    static {
        postgres = new PostgreSQLContainer("postgres:16")
                .withDatabaseName("shopapi_test")
                .withUsername("shopapi")
                .withPassword("shopapi");
        postgres.start();

        redis = new GenericContainer<>("redis:7")
                .withExposedPorts(6379);
        redis.start();

        rabbitmq = new RabbitMQContainer("rabbitmq:4-management");
        rabbitmq.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
    }
}