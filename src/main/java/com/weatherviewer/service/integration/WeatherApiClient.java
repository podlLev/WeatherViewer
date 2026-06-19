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

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherApiClient {

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

    public JsonNode fetchCurrentWeatherByCity(String city) {
        log.debug("Building URL and fetching current weather for city: {}", city);
        String url = buildCityUrl(weatherApiUrlSuffix, city);
        return fetchJsonNode(url);
    }

    public JsonNode fetchCurrentWeatherByCoordinates(double latitude, double longitude) {
        log.debug("Building URL and fetching current weather for lat: {}, lon: {}", latitude, longitude);
        String url = buildCoordsUrl(weatherApiUrlSuffix, latitude, longitude);
        return fetchJsonNode(url);
    }

    public JsonNode fetchForecastByCity(String city) {
        log.debug("Building URL and fetching forecast for city: {}", city);
        String url = buildCityUrl(forecastApiUrlSuffix, city);
        return fetchJsonNode(url);
    }

    public JsonNode fetchForecastByCoordinates(double latitude, double longitude) {
        log.debug("Building URL and fetching forecast for lat: {}, lon: {}", latitude, longitude);
        String url = buildCoordsUrl(forecastApiUrlSuffix, latitude, longitude);
        return fetchJsonNode(url);
    }

    public JsonNode fetchGeocodingByCity(String city) {
        log.debug("Building URL and fetching geocoding data for city: {}", city);
        String url = buildCityUrl(geocodingApiUrlSuffix, city);
        return fetchJsonNode(url);
    }

    private JsonNode fetchJsonNode(String url) {
        try {
            String body = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(String.class);
            return objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            String message = "Failed to parse JSON response from weather API service";
            log.error("{} for URL: {}", message, url, e);
            throw new ExternalHttpCallException(message);
        } catch (RestClientResponseException e) {
            log.error("Weather API returned {} for URL: {}", e.getStatusCode(), url, e);
            throw new ExternalHttpCallException("Weather API error: " + e.getStatusCode());
        } catch (Exception e) {
            String message = "External HTTP call failed due to network or connection issues";
            log.error("{} for URL: {}", message, url, e);
            throw new ExternalHttpCallException(message);
        }
    }

    private String buildCityUrl(String suffix, String city) {
        return UriComponentsBuilder.fromUri(URI.create(baseApiUrl + suffix))
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("lang", "en")
                .queryParam("q", city)
                .build()
                .toUriString();
    }

    private String buildCoordsUrl(String suffix, double latitude, double longitude) {
        return UriComponentsBuilder.fromUri(URI.create(baseApiUrl + suffix))
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("lang", "en")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .build()
                .toUriString();
    }

}
