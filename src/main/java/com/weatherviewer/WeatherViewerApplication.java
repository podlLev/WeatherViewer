package com.weatherviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WeatherViewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherViewerApplication.class, args);
    }

}
