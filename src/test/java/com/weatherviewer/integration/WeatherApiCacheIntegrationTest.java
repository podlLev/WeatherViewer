package com.weatherviewer.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.service.integration.WeatherApiCache;
import com.weatherviewer.service.integration.WeatherApiClient;
import com.weatherviewer.testcontainers.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Runs against a real Redis cache (via {@link TestcontainersConfiguration}),
 * not an in-memory {@code simple} cache — so a cached value actually has to
 * survive a serialize/deserialize round trip to come back on a cache hit,
 * the way it would in production. That's why every cached value here is a
 * real, Jackson-parsed {@link JsonNode} rather than a Mockito mock: a mock
 * has nothing meaningful to serialize and would fail (or silently prove
 * nothing) the moment the cache manager tries to write it to Redis.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class WeatherApiCacheIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    private JsonNode weatherNode(String city) throws Exception {
        return objectMapper.readTree("""
                {"name":"%s","main":{"temp":21.5},"weather":[{"main":"Clear"}]}
                """.formatted(city));
    }

    @Test
    void fetchCurrentWeatherByCity_cachedOnSecondCall() throws Exception {
        JsonNode realNode = weatherNode("Kyiv");
        when(weatherApiClient.fetchCurrentWeatherByCity("Kyiv")).thenReturn(realNode);

        JsonNode first = weatherApiCache.fetchCurrentWeatherByCity("Kyiv");
        JsonNode second = weatherApiCache.fetchCurrentWeatherByCity("Kyiv");

        verify(weatherApiClient, times(1)).fetchCurrentWeatherByCity("Kyiv");

        assertThat(second).isEqualTo(realNode);
        assertThat(second).isNotSameAs(first);
    }

    @Test
    void fetchCurrentWeatherByCoordinates_cachedOnSecondCall() throws Exception {
        JsonNode realNode = weatherNode("Kyiv");
        when(weatherApiClient.fetchCurrentWeatherByCoordinates(50.45, 30.52)).thenReturn(realNode);

        weatherApiCache.fetchCurrentWeatherByCoordinates(50.45, 30.52);
        weatherApiCache.fetchCurrentWeatherByCoordinates(50.45, 30.52);

        verify(weatherApiClient, times(1)).fetchCurrentWeatherByCoordinates(50.45, 30.52);
    }

    @Test
    void fetchForecastByCity_cachedOnSecondCall() throws Exception {
        JsonNode realNode = weatherNode("Kyiv");
        when(weatherApiClient.fetchForecastByCity("Kyiv")).thenReturn(realNode);

        weatherApiCache.fetchForecastByCity("Kyiv");
        weatherApiCache.fetchForecastByCity("Kyiv");

        verify(weatherApiClient, times(1)).fetchForecastByCity("Kyiv");
    }

    @Test
    void fetchForecastByCoordinates_cachedOnSecondCall() throws Exception {
        JsonNode realNode = weatherNode("Kyiv");
        when(weatherApiClient.fetchForecastByCoordinates(50.45, 30.52)).thenReturn(realNode);

        weatherApiCache.fetchForecastByCoordinates(50.45, 30.52);
        weatherApiCache.fetchForecastByCoordinates(50.45, 30.52);

        verify(weatherApiClient, times(1)).fetchForecastByCoordinates(50.45, 30.52);
    }

    @Test
    void fetchGeocodingByCity_cachedOnSecondCall() throws Exception {
        JsonNode realNode = weatherNode("Kyiv");
        when(weatherApiClient.fetchGeocodingByCity("Kyiv")).thenReturn(realNode);

        weatherApiCache.fetchGeocodingByCity("Kyiv");
        weatherApiCache.fetchGeocodingByCity("Kyiv");

        verify(weatherApiClient, times(1)).fetchGeocodingByCity("Kyiv");
    }

    @Test
    void fetchCurrentWeatherByCity_differentCities_notCached() throws Exception {
        when(weatherApiClient.fetchCurrentWeatherByCity(anyString()))
                .thenAnswer(invocation -> weatherNode(invocation.getArgument(0)));

        weatherApiCache.fetchCurrentWeatherByCity("Kyiv");
        weatherApiCache.fetchCurrentWeatherByCity("Lviv");

        verify(weatherApiClient, times(1)).fetchCurrentWeatherByCity("Kyiv");
        verify(weatherApiClient, times(1)).fetchCurrentWeatherByCity("Lviv");
    }

    @Test
    void fetchCurrentWeatherByCoordinates_differentCoords_notCached() throws Exception {
        JsonNode realNode = weatherNode("Kyiv");
        when(weatherApiClient.fetchCurrentWeatherByCoordinates(anyDouble(), anyDouble())).thenReturn(realNode);

        weatherApiCache.fetchCurrentWeatherByCoordinates(50.45, 30.52);
        weatherApiCache.fetchCurrentWeatherByCoordinates(48.92, 24.71);

        verify(weatherApiClient, times(1)).fetchCurrentWeatherByCoordinates(50.45, 30.52);
        verify(weatherApiClient, times(1)).fetchCurrentWeatherByCoordinates(48.92, 24.71);
    }

}
