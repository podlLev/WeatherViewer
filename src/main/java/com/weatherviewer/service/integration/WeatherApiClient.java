package com.weatherviewer.service.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.exception.ExternalHttpCallException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Thin HTTP client for the OpenWeatherMap API (current weather, forecast,
 * and geocoding endpoints). Returns raw {@link JsonNode} trees rather than
 * typed objects, since {@link com.weatherviewer.mapper.WeatherApiMapper}
 * extracts only the fields this application needs from the provider's
 * larger response payload.
 * <p>
 * Every failure mode (malformed JSON, a non-2xx response, or a network
 * error) is normalized into an {@link ExternalHttpCallException}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherApiClient {

    private static final Pattern APPID_PATTERN = Pattern.compile("(?i)([?&]appid=)[^&]*");

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.base.api.url}")
    private String baseApiUrl;

    @Value("${weather.api.url.suffix}")
    private String weatherApiUrlSuffix;

    @Value("${forecast.api.url.suffix}")
    private String forecastApiUrlSuffix;

    @Value("${geo.api.url.suffix}")
    private String geocodingApiUrlSuffix;

    /** Fetches current weather for a city by name. */
    public JsonNode fetchCurrentWeatherByCity(String city) {
        log.debug("Building URL and fetching current weather for city: {}", city);
        String url = buildCityUrl(weatherApiUrlSuffix, city);
        return fetchJsonNode(url);
    }

    /** Fetches current weather for a coordinate pair. */
    public JsonNode fetchCurrentWeatherByCoordinates(double latitude, double longitude) {
        log.debug("Building URL and fetching current weather for lat: {}, lon: {}", latitude, longitude);
        String url = buildCoordsUrl(weatherApiUrlSuffix, latitude, longitude);
        return fetchJsonNode(url);
    }

    /** Fetches the raw 3-hour-step forecast for a city by name. */
    public JsonNode fetchForecastByCity(String city) {
        log.debug("Building URL and fetching forecast for city: {}", city);
        String url = buildCityUrl(forecastApiUrlSuffix, city);
        return fetchJsonNode(url);
    }

    /** Fetches the raw 3-hour-step forecast for a coordinate pair. */
    public JsonNode fetchForecastByCoordinates(double latitude, double longitude) {
        log.debug("Building URL and fetching forecast for lat: {}, lon: {}", latitude, longitude);
        String url = buildCoordsUrl(forecastApiUrlSuffix, latitude, longitude);
        return fetchJsonNode(url);
    }

    /** Geocodes a free-text city name into candidate locations. */
    public JsonNode fetchGeocodingByCity(String city) {
        log.debug("Building URL and fetching geocoding data for city: {}", city);
        String url = buildCityUrl(geocodingApiUrlSuffix, city);
        return fetchJsonNode(url);
    }

    /**
     * Issues the GET request and parses the response body as JSON.
     *
     * @throws ExternalHttpCallException wrapping any parsing, HTTP status, or network failure
     */
    private JsonNode fetchJsonNode(String url) {
        try {
            String body = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(String.class);
            return objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            String message = "Failed to parse JSON response from weather API service";
            log.error("{} for URL: {}", message, maskApiKey(url), e);
            throw new ExternalHttpCallException(message);
        } catch (RestClientResponseException e) {
            log.error("Weather API returned {} for URL: {}", e.getStatusCode(), maskApiKey(url), e);
            throw new ExternalHttpCallException("Weather API error: " + e.getStatusCode());
        } catch (Exception e) {
            String message = "External HTTP call failed due to network or connection issues";
            log.error("{} for URL: {}", message, maskApiKey(url), e);
            throw new ExternalHttpCallException(message);
        }
    }

    /** Masks the {@code appid} query parameter so the API key never reaches application logs. */
    private static String maskApiKey(String url) {
        return APPID_PATTERN.matcher(url).replaceAll("$1***");
    }

    /** Builds a request URL for a city-name-based endpoint, with metric units and English descriptions. */
    private String buildCityUrl(String suffix, String city) {
        return UriComponentsBuilder.fromUri(URI.create(baseApiUrl + suffix))
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("lang", "en")
                .queryParam("q", city)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
    }

    /** Builds a request URL for a coordinate-based endpoint, with metric units and English descriptions. */
    private String buildCoordsUrl(String suffix, double latitude, double longitude) {
        return UriComponentsBuilder.fromUri(URI.create(baseApiUrl + suffix))
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("lang", "en")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
    }

}
