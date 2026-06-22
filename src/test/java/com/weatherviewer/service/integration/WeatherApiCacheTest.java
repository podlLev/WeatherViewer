package com.weatherviewer.service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherApiCacheTest {

    @Mock
    private WeatherApiClient weatherApiClient;

    @InjectMocks
    private WeatherApiCache weatherApiCache;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode json(String raw) throws Exception {
        return objectMapper.readTree(raw);
    }

    @Test
    void fetchCurrentWeatherByCity_delegatesToClient() throws Exception {
        JsonNode node = json("{\"weather\": []}");
        when(weatherApiClient.fetchCurrentWeatherByCity("Kyiv")).thenReturn(node);

        JsonNode result = weatherApiCache.fetchCurrentWeatherByCity("Kyiv");

        assertThat(result).isEqualTo(node);
        verify(weatherApiClient).fetchCurrentWeatherByCity("Kyiv");
    }

    @Test
    void fetchCurrentWeatherByCoordinates_delegatesToClient() throws Exception {
        JsonNode node = json("{\"weather\": []}");
        when(weatherApiClient.fetchCurrentWeatherByCoordinates(50.45, 30.52)).thenReturn(node);

        JsonNode result = weatherApiCache.fetchCurrentWeatherByCoordinates(50.45, 30.52);

        assertThat(result).isEqualTo(node);
        verify(weatherApiClient).fetchCurrentWeatherByCoordinates(50.45, 30.52);
    }

    @Test
    void fetchForecastByCity_delegatesToClient() throws Exception {
        JsonNode node = json("{\"list\": []}");
        when(weatherApiClient.fetchForecastByCity("Kyiv")).thenReturn(node);

        JsonNode result = weatherApiCache.fetchForecastByCity("Kyiv");

        assertThat(result).isEqualTo(node);
        verify(weatherApiClient).fetchForecastByCity("Kyiv");
    }

    @Test
    void fetchForecastByCoordinates_delegatesToClient() throws Exception {
        JsonNode node = json("{\"list\": []}");
        when(weatherApiClient.fetchForecastByCoordinates(50.45, 30.52)).thenReturn(node);

        JsonNode result = weatherApiCache.fetchForecastByCoordinates(50.45, 30.52);

        assertThat(result).isEqualTo(node);
        verify(weatherApiClient).fetchForecastByCoordinates(50.45, 30.52);
    }

    @Test
    void fetchGeocodingByCity_delegatesToClient() throws Exception {
        JsonNode node = json("[{\"name\": \"Kyiv\"}]");
        when(weatherApiClient.fetchGeocodingByCity("Kyiv")).thenReturn(node);

        JsonNode result = weatherApiCache.fetchGeocodingByCity("Kyiv");

        assertThat(result).isEqualTo(node);
        verify(weatherApiClient).fetchGeocodingByCity("Kyiv");
    }

}
