package com.weatherviewer.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.weatherviewer.service.integration.WeatherApiCache;
import com.weatherviewer.service.integration.WeatherApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Objects;

import static org.mockito.Mockito.*;

@SpringBootTest
class WeatherApiCacheIntegrationTest {

    @MockitoBean
    private WeatherApiClient weatherApiClient;

    @Autowired
    private WeatherApiCache weatherApiCache;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames()
                .forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }

    @Test
    void fetchCurrentWeatherByCity_cachedOnSecondCall() {
        JsonNode mockNode = mock(JsonNode.class);
        when(weatherApiClient.fetchCurrentWeatherByCity("Kyiv")).thenReturn(mockNode);

        weatherApiCache.fetchCurrentWeatherByCity("Kyiv");
        weatherApiCache.fetchCurrentWeatherByCity("Kyiv");

        verify(weatherApiClient, times(1)).fetchCurrentWeatherByCity("Kyiv");
    }

    @Test
    void fetchCurrentWeatherByCoordinates_cachedOnSecondCall() {
        JsonNode mockNode = mock(JsonNode.class);
        when(weatherApiClient.fetchCurrentWeatherByCoordinates(50.45, 30.52)).thenReturn(mockNode);

        weatherApiCache.fetchCurrentWeatherByCoordinates(50.45, 30.52);
        weatherApiCache.fetchCurrentWeatherByCoordinates(50.45, 30.52);

        verify(weatherApiClient, times(1)).fetchCurrentWeatherByCoordinates(50.45, 30.52);
    }

    @Test
    void fetchForecastByCity_cachedOnSecondCall() {
        JsonNode mockNode = mock(JsonNode.class);
        when(weatherApiClient.fetchForecastByCity("Kyiv")).thenReturn(mockNode);

        weatherApiCache.fetchForecastByCity("Kyiv");
        weatherApiCache.fetchForecastByCity("Kyiv");

        verify(weatherApiClient, times(1)).fetchForecastByCity("Kyiv");
    }

    @Test
    void fetchForecastByCoordinates_cachedOnSecondCall() {
        JsonNode mockNode = mock(JsonNode.class);
        when(weatherApiClient.fetchForecastByCoordinates(50.45, 30.52)).thenReturn(mockNode);

        weatherApiCache.fetchForecastByCoordinates(50.45, 30.52);
        weatherApiCache.fetchForecastByCoordinates(50.45, 30.52);

        verify(weatherApiClient, times(1)).fetchForecastByCoordinates(50.45, 30.52);
    }

    @Test
    void fetchGeocodingByCity_cachedOnSecondCall() {
        JsonNode mockNode = mock(JsonNode.class);
        when(weatherApiClient.fetchGeocodingByCity("Kyiv")).thenReturn(mockNode);

        weatherApiCache.fetchGeocodingByCity("Kyiv");
        weatherApiCache.fetchGeocodingByCity("Kyiv");

        verify(weatherApiClient, times(1)).fetchGeocodingByCity("Kyiv");
    }

    @Test
    void fetchCurrentWeatherByCity_differentCities_notCached() {
        JsonNode mockNode = mock(JsonNode.class);
        when(weatherApiClient.fetchCurrentWeatherByCity(any())).thenReturn(mockNode);

        weatherApiCache.fetchCurrentWeatherByCity("Kyiv");
        weatherApiCache.fetchCurrentWeatherByCity("Lviv");

        verify(weatherApiClient, times(1)).fetchCurrentWeatherByCity("Kyiv");
        verify(weatherApiClient, times(1)).fetchCurrentWeatherByCity("Lviv");
    }

    @Test
    void fetchCurrentWeatherByCoordinates_differentCoords_notCached() {
        JsonNode mockNode = mock(JsonNode.class);
        when(weatherApiClient.fetchCurrentWeatherByCoordinates(anyDouble(), anyDouble())).thenReturn(mockNode);

        weatherApiCache.fetchCurrentWeatherByCoordinates(50.45, 30.52);
        weatherApiCache.fetchCurrentWeatherByCoordinates(48.92, 24.71);

        verify(weatherApiClient, times(1)).fetchCurrentWeatherByCoordinates(50.45, 30.52);
        verify(weatherApiClient, times(1)).fetchCurrentWeatherByCoordinates(48.92, 24.71);
    }

}
