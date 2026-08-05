package com.weatherviewer;

import com.weatherviewer.testcontainers.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class WeatherViewerApplicationIT {

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

    @Autowired
    private GenericContainer<?> redisContainer;

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethodStartsApplication() {
        WeatherViewerApplication.main(new String[] {
                "--spring.datasource.url=" + postgresContainer.getJdbcUrl(),
                "--spring.datasource.username=" + postgresContainer.getUsername(),
                "--spring.datasource.password=" + postgresContainer.getPassword(),
                "--spring.datasource.driver-class-name=org.postgresql.Driver",
                "--spring.data.redis.host=" + redisContainer.getHost(),
                "--spring.data.redis.port=" + redisContainer.getMappedPort(6379),
                "--server.port=0",
        });
    }

}
