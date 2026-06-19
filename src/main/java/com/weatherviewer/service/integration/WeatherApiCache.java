package com.weatherviewer.service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherApiCache {

    private final WeatherApiClient weatherApiClient;

    @Cacheable(value = "weatherCache", key = "'city:' + #city")
    public JsonNode fetchCurrentWeatherByCity(String city) {
        return weatherApiClient.fetchCurrentWeatherByCity(city);
    }

    @Cacheable(value = "weatherCache", key = "'coords:' + #latitude + ':' + #longitude")
    public JsonNode fetchCurrentWeatherByCoordinates(double latitude, double longitude) {
        return weatherApiClient.fetchCurrentWeatherByCoordinates(latitude, longitude);
    }

    @Cacheable(value = "forecastCache", key = "'city:' + #city")
    public JsonNode fetchForecastByCity(String city) {
        return weatherApiClient.fetchForecastByCity(city);
    }

    @Cacheable(value = "forecastCache", key = "'coords:' + #latitude + ':' + #longitude")
    public JsonNode fetchForecastByCoordinates(double latitude, double longitude) {
        return weatherApiClient.fetchForecastByCoordinates(latitude, longitude);
    }

    @Cacheable(value = "geoCache", key = "#city")
    public JsonNode fetchGeocodingByCity(String city) {
        return weatherApiClient.fetchGeocodingByCity(city);
    }

}
