package com.weatherviewer.testcontainers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Boots real Postgres and Redis containers for integration tests, instead of
 * H2 (Postgres compatibility mode) and {@code spring.cache.type=simple}.
 * <p>
 * Both containers are wired via Spring Boot's {@link ServiceConnection}, so
 * no {@code spring.datasource.*} or {@code spring.data.redis.*} properties
 * need to be set by hand — Spring Boot autoconfigures the datasource and
 * Redis connection factory directly from the running containers.
 * <p>
 * Import this into a test with {@code @Import(TestcontainersConfiguration.class)}.
 * Spring's test context caching means all tests that import this identical
 * configuration share the same pair of containers instead of starting a new per test.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
    }

    @Bean
    @ServiceConnection("redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
    }

}
