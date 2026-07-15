package com.weatherviewer.service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Caching facade in front of {@link WeatherApiClient}.
 * <p>
 * Each method mirrors a client call but is annotated with
 * {@link Cacheable @Cacheable}, so repeated lookups for the same
 * city/coordinates are served from the cache (Redis in production,
 * in-memory for local/dev — see the {@code spring.cache.type} property)
 * instead of hitting OpenWeatherMap again. Cache keys are namespaced per
 * cache region ({@code weatherCache}, {@code forecastCache}, {@code geoCache})
 * and per lookup type (city vs. coordinates) to avoid collisions.
 */
@Component
@RequiredArgsConstructor
public class WeatherApiCache {

    private final WeatherApiClient weatherApiClient;

    /** Cached current-weather lookup by city name. */
    @Cacheable(value = "weatherCache", key = "'city:' + #city")
    public JsonNode fetchCurrentWeatherByCity(String city) {
        return weatherApiClient.fetchCurrentWeatherByCity(city);
    }

    /** Cached current-weather lookup by coordinates. */
    @Cacheable(value = "weatherCache", key = "'coords:' + #latitude + ':' + #longitude")
    public JsonNode fetchCurrentWeatherByCoordinates(double latitude, double longitude) {
        return weatherApiClient.fetchCurrentWeatherByCoordinates(latitude, longitude);
    }

    /** Cached 3-hour-step forecast lookup by city name. */
    @Cacheable(value = "forecastCache", key = "'city:' + #city")
    public JsonNode fetchForecastByCity(String city) {
        return weatherApiClient.fetchForecastByCity(city);
    }

    /** Cached 3-hour-step forecast lookup by coordinates. */
    @Cacheable(value = "forecastCache", key = "'coords:' + #latitude + ':' + #longitude")
    public JsonNode fetchForecastByCoordinates(double latitude, double longitude) {
        return weatherApiClient.fetchForecastByCoordinates(latitude, longitude);
    }

    /** Cached city-name geocoding lookup. */
    @Cacheable(value = "geoCache", key = "#city")
    public JsonNode fetchGeocodingByCity(String city) {
        return weatherApiClient.fetchGeocodingByCity(city);
    }

}
