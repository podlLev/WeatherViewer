package com.weatherviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.TimeZone;

/**
 * Entry point for the WeatherViewer Spring Boot application.
 * <p>
 * Bootstraps the application context and enables Spring's caching
 * abstraction ({@link EnableCaching}), which backs the weather/forecast/
 * geocoding response caching in
 * {@link com.weatherviewer.service.integration.WeatherApiCache}.
 */
@SpringBootApplication
@EnableCaching
public class WeatherViewerApplication {

    /**
     * Application entry point. Forces the JVM default time zone to UTC
     * before starting Spring so that all date/time handling (JPA timestamps,
     * scheduling, logging) is consistent regardless of the host's local
     * time zone, then launches the Spring Boot application context.
     *
     * @param args command-line arguments passed through to Spring Boot
     */
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(WeatherViewerApplication.class, args);
    }

}
