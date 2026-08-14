package com.maleyk.subscription_service;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class BaseIntegrationTest {

    protected static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    protected static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));

    static {
        postgres.start();
        kafka.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        registry.add("eureka.client.enabled", () -> "false");
    }
}