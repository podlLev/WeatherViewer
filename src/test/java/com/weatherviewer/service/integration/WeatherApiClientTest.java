package com.weatherviewer.service.integration;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.exception.ExternalHttpCallException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherApiClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WeatherApiClient client;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private static final String SECRET_API_KEY = "test-api-key-super-secret";

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "apiKey", SECRET_API_KEY);
        ReflectionTestUtils.setField(client, "baseApiUrl", "https://api.openweathermap.org");
        ReflectionTestUtils.setField(client, "weatherApiUrlSuffix", "/data/2.5/weather");
        ReflectionTestUtils.setField(client, "forecastApiUrlSuffix", "/data/2.5/forecast");
        ReflectionTestUtils.setField(client, "geocodingApiUrlSuffix", "/geo/1.0/direct");

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(URI.class));
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        logAppender = new ListAppender<>();
        logAppender.start();
        ((Logger) LoggerFactory.getLogger(WeatherApiClient.class)).addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        ((Logger) LoggerFactory.getLogger(WeatherApiClient.class)).detachAppender(logAppender);
    }

    @Test
    void fetchCurrentWeatherByCity_returnsJsonNode() throws Exception {
        JsonNode mockNode = mock(JsonNode.class);
        when(responseSpec.body(String.class)).thenReturn("{\"weather\": []}");
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        JsonNode result = client.fetchCurrentWeatherByCity("Kyiv");

        assertThat(result).isEqualTo(mockNode);
    }

    @Test
    void fetchCurrentWeatherByCoordinates_returnsJsonNode() throws Exception {
        JsonNode mockNode = mock(JsonNode.class);
        when(responseSpec.body(String.class)).thenReturn("{\"weather\": []}");
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        JsonNode result = client.fetchCurrentWeatherByCoordinates(50.45, 30.52);

        assertThat(result).isEqualTo(mockNode);
    }

    @Test
    void fetchForecastByCity_returnsJsonNode() throws Exception {
        JsonNode mockNode = mock(JsonNode.class);
        when(responseSpec.body(String.class)).thenReturn("{\"list\": []}");
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        JsonNode result = client.fetchForecastByCity("Kyiv");

        assertThat(result).isEqualTo(mockNode);
    }

    @Test
    void fetchForecastByCoordinates_returnsJsonNode() throws Exception {
        JsonNode mockNode = mock(JsonNode.class);
        when(responseSpec.body(String.class)).thenReturn("{\"list\": []}");
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        JsonNode result = client.fetchForecastByCoordinates(50.45, 30.52);

        assertThat(result).isEqualTo(mockNode);
    }

    @Test
    void fetchGeocodingByCity_returnsJsonNode() throws Exception {
        JsonNode mockNode = mock(JsonNode.class);
        when(responseSpec.body(String.class)).thenReturn("[]");
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        JsonNode result = client.fetchGeocodingByCity("Kyiv");

        assertThat(result).isEqualTo(mockNode);
    }

    @Test
    void fetchCurrentWeatherByCity_encodesCityNameExactlyOnce() throws Exception {
        JsonNode mockNode = mock(JsonNode.class);
        when(responseSpec.body(String.class)).thenReturn("{\"weather\": []}");
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        client.fetchCurrentWeatherByCity("São Paulo");

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(requestHeadersUriSpec).uri(uriCaptor.capture());

        String requestedUri = uriCaptor.getValue().toString();
        assertThat(requestedUri).contains("q=S%C3%A3o%20Paulo");
        assertThat(requestedUri).doesNotContain("%25");
    }

    @Test
    void fetchJsonNode_jsonProcessingException_throwsExternalHttpCallException() throws Exception {
        when(responseSpec.body(String.class)).thenReturn("invalid-json");
        when(objectMapper.readTree(anyString()))
                .thenThrow(new JsonProcessingException("parse error") {});

        assertThatThrownBy(() -> client.fetchCurrentWeatherByCity("Kyiv"))
                .isInstanceOf(ExternalHttpCallException.class)
                .hasMessageContaining("Failed to parse JSON response");
    }

    @Test
    void fetchJsonNode_restClientResponseException_throwsExternalHttpCallException() {
        when(responseSpec.body(String.class))
                .thenThrow(mock(RestClientResponseException.class));

        assertThatThrownBy(() -> client.fetchCurrentWeatherByCity("Kyiv"))
                .isInstanceOf(ExternalHttpCallException.class)
                .hasMessageContaining("Weather API error");
    }

    @Test
    void fetchJsonNode_networkException_throwsExternalHttpCallException() {
        when(responseSpec.body(String.class))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> client.fetchCurrentWeatherByCity("Kyiv"))
                .isInstanceOf(ExternalHttpCallException.class)
                .hasMessageContaining("External HTTP call failed");
    }

    @Test
    void fetchJsonNode_jsonProcessingException_doesNotLogApiKey() throws Exception {
        when(responseSpec.body(String.class)).thenReturn("invalid-json");
        when(objectMapper.readTree(anyString()))
                .thenThrow(new JsonProcessingException("parse error") {});

        assertThatThrownBy(() -> client.fetchCurrentWeatherByCity("Kyiv"))
                .isInstanceOf(ExternalHttpCallException.class);

        assertLoggedMessagesContainNoApiKeyAndAreMasked();
    }

    @Test
    void fetchJsonNode_restClientResponseException_doesNotLogApiKey() {
        RestClientResponseException ex = mock(RestClientResponseException.class);
        when(responseSpec.body(String.class)).thenThrow(ex);

        assertThatThrownBy(() -> client.fetchCurrentWeatherByCity("Kyiv"))
                .isInstanceOf(ExternalHttpCallException.class);

        assertLoggedMessagesContainNoApiKeyAndAreMasked();
    }

    @Test
    void fetchJsonNode_networkException_doesNotLogApiKey() {
        when(responseSpec.body(String.class))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> client.fetchCurrentWeatherByCity("Kyiv"))
                .isInstanceOf(ExternalHttpCallException.class);

        assertLoggedMessagesContainNoApiKeyAndAreMasked();
    }

    @Test
    void fetchJsonNode_errorLogging_masksApiKeyRegardlessOfSuffixOrParamOrder() {
        when(responseSpec.body(String.class))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> client.fetchCurrentWeatherByCoordinates(50.45, 30.52))
                .isInstanceOf(ExternalHttpCallException.class);

        assertLoggedMessagesContainNoApiKeyAndAreMasked();
    }

    private void assertLoggedMessagesContainNoApiKeyAndAreMasked() {
        List<ILoggingEvent> events = logAppender.list;
        assertThat(events).isNotEmpty();

        for (ILoggingEvent event : events) {
            assertThat(event.getFormattedMessage()).doesNotContain(SECRET_API_KEY);
        }

        assertThat(events).anyMatch(event -> event.getFormattedMessage().contains("appid=***"));
    }

}
