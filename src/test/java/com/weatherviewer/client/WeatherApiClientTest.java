package com.weatherviewer.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.exception.ExternalHttpCallException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WeatherApiClient client;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(client, "baseApiUrl", "https://api.openweathermap.org");
        ReflectionTestUtils.setField(client, "weatherApiUrlSuffix", "/data/2.5/weather");
        ReflectionTestUtils.setField(client, "forecastApiUrlSuffix", "/data/2.5/forecast");
        ReflectionTestUtils.setField(client, "geocodingApiUrlSuffix", "/geo/1.0/direct");
    }

    @Test
    void fetchCurrentWeatherByCity_returnsJsonNode() throws Exception {
        JsonNode mockNode = mock(JsonNode.class);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"weather\": []}"));
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        JsonNode result = client.fetchCurrentWeatherByCity("Kyiv");

        assertThat(result).isEqualTo(mockNode);
    }

    @Test
    void fetchCurrentWeatherByCoordinates_returnsJsonNode() throws Exception {
        JsonNode mockNode = mock(JsonNode.class);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"weather\": []}"));
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        JsonNode result = client.fetchCurrentWeatherByCoordinates(50.45, 30.52);

        assertThat(result).isEqualTo(mockNode);
    }

    @Test
    void fetchForecastByCity_returnsJsonNode() throws Exception {
        JsonNode mockNode = mock(JsonNode.class);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"list\": []}"));
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        JsonNode result = client.fetchForecastByCity("Kyiv");

        assertThat(result).isEqualTo(mockNode);
    }

    @Test
    void fetchForecastByCoordinates_returnsJsonNode() throws Exception {
        JsonNode mockNode = mock(JsonNode.class);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"list\": []}"));
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        JsonNode result = client.fetchForecastByCoordinates(50.45, 30.52);

        assertThat(result).isEqualTo(mockNode);
    }

    @Test
    void fetchGeocodingByCity_returnsJsonNode() throws Exception {
        JsonNode mockNode = mock(JsonNode.class);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("[]"));
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        JsonNode result = client.fetchGeocodingByCity("Kyiv");

        assertThat(result).isEqualTo(mockNode);
    }

    @Test
    void fetchJsonNode_jsonProcessingException_throwsExternalHttpCallException() throws Exception {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("invalid-json"));
        when(objectMapper.readTree(anyString()))
                .thenThrow(new JsonProcessingException("parse error") {});

        assertThatThrownBy(() -> client.fetchCurrentWeatherByCity("Kyiv"))
                .isInstanceOf(ExternalHttpCallException.class)
                .hasMessageContaining("Failed to parse JSON response");
    }

    @Test
    void fetchJsonNode_networkException_throwsExternalHttpCallException() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> client.fetchCurrentWeatherByCity("Kyiv"))
                .isInstanceOf(ExternalHttpCallException.class)
                .hasMessageContaining("External HTTP call failed");
    }

}
