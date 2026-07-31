package com.weatherviewer.service.integration;

import com.weatherviewer.exception.ExternalHttpCallException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherTileClientTest {

    private static final String SECRET_API_KEY = "test-api-key-super-secret";

    @Mock
    private RestClient restClient;

    @InjectMocks
    private WeatherTileClient client;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "apiKey", SECRET_API_KEY);
        ReflectionTestUtils.setField(client, "tileBaseUrl", "https://tile.openweathermap.org/map");

        lenient().doReturn(requestHeadersUriSpec).when(restClient).get();
        lenient().doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(URI.class));
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void fetchTile_returnsBytesFromClient() {
        byte[] pngBytes = new byte[]{1, 2, 3, 4};
        when(responseSpec.body(byte[].class)).thenReturn(pngBytes);

        byte[] result = client.fetchTile(MapTileLayer.CLOUDS, 5, 10, 12);

        assertThat(result).isEqualTo(pngBytes);
    }

    @Test
    void fetchTile_buildsUrlWithOwmCodeAndAppid() {
        when(responseSpec.body(byte[].class)).thenReturn(new byte[]{1});

        client.fetchTile(MapTileLayer.PRECIPITATION, 5, 10, 12);

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(requestHeadersUriSpec).uri(uriCaptor.capture());
        String uri = uriCaptor.getValue().toString();

        assertThat(uri).startsWith("https://tile.openweathermap.org/map/precipitation_new/5/10/12.png");
        assertThat(uri).contains("appid=" + SECRET_API_KEY);
    }

    @Test
    void fetchTile_apiKeyNeverAppearsInThrownExceptionMessage() {
        RestClientResponseException upstreamException = mock(RestClientResponseException.class);
        when(upstreamException.getStatusCode()).thenReturn(HttpStatusCode.valueOf(500));
        when(responseSpec.body(byte[].class)).thenThrow(upstreamException);

        assertThatThrownBy(() -> client.fetchTile(MapTileLayer.WIND, 1, 1, 1))
                .isInstanceOf(ExternalHttpCallException.class)
                .hasMessageNotContaining(SECRET_API_KEY);
    }

    @Test
    void fetchTile_upstream5xx_isRetryable() {
        RestClientResponseException upstreamException = mock(RestClientResponseException.class);
        when(upstreamException.getStatusCode()).thenReturn(HttpStatusCode.valueOf(503));
        when(responseSpec.body(byte[].class)).thenThrow(upstreamException);

        assertThatThrownBy(() -> client.fetchTile(MapTileLayer.WIND, 1, 1, 1))
                .isInstanceOfSatisfying(ExternalHttpCallException.class,
                        ex -> assertThat(ex.isRetryable()).isTrue());
    }

    @Test
    void fetchTile_upstream4xx_isNotRetryable() {
        RestClientResponseException upstreamException = mock(RestClientResponseException.class);
        when(upstreamException.getStatusCode()).thenReturn(HttpStatusCode.valueOf(404));
        when(responseSpec.body(byte[].class)).thenThrow(upstreamException);

        assertThatThrownBy(() -> client.fetchTile(MapTileLayer.WIND, 1, 1, 1))
                .isInstanceOfSatisfying(ExternalHttpCallException.class,
                        ex -> assertThat(ex.isRetryable()).isFalse());
    }

    @Test
    void fetchTile_upstreamStatusCodeNull_isRetryable() {
        RestClientResponseException upstreamException = mock(RestClientResponseException.class);
        when(upstreamException.getStatusCode()).thenReturn(null);
        when(responseSpec.body(byte[].class)).thenThrow(upstreamException);

        assertThatThrownBy(() -> client.fetchTile(MapTileLayer.WIND, 1, 1, 1))
                .isInstanceOfSatisfying(ExternalHttpCallException.class,
                        ex -> assertThat(ex.isRetryable()).isTrue());
    }

    @Test
    void fetchTile_networkFailure_wrappedAsExternalHttpCallException() {
        when(responseSpec.body(byte[].class)).thenThrow(new RuntimeException("connection reset"));

        assertThatThrownBy(() -> client.fetchTile(MapTileLayer.TEMPERATURE, 2, 2, 2))
                .isInstanceOf(ExternalHttpCallException.class);
    }

}
