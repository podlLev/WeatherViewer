package com.weatherviewer.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.exception.ExternalHttpCallException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherApiClient {

    private final RestTemplate restTemplate;
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
        String url = buildUrl(weatherApiUrlSuffix, "&q=" + city);
        return fetchJsonNode(url);
    }

    public JsonNode fetchCurrentWeatherByCoordinates(double latitude, double longitude) {
        log.debug("Building URL and fetching current weather for lat: {}, lon: {}", latitude, longitude);
        String url = buildUrl(weatherApiUrlSuffix, String.format("&lat=%f&lon=%f", latitude, longitude));
        return fetchJsonNode(url);
    }

    public JsonNode fetchForecastByCity(String city) {
        log.debug("Building URL and fetching forecast for city: {}", city);
        String url = buildUrl(forecastApiUrlSuffix, "&q=" + city);
        return fetchJsonNode(url);
    }

    public JsonNode fetchForecastByCoordinates(double latitude, double longitude) {
        log.debug("Building URL and fetching forecast for lat: {}, lon: {}", latitude, longitude);
        String url = buildUrl(forecastApiUrlSuffix, String.format("&lat=%f&lon=%f", latitude, longitude));
        return fetchJsonNode(url);
    }

    public JsonNode fetchGeocodingByCity(String city) {
        log.debug("Building URL and fetching geocoding data for city: {}", city);
        String url = buildUrl(geocodingApiUrlSuffix, "&q=" + city + "&limit=10");
        return fetchJsonNode(url);
    }

    private JsonNode fetchJsonNode(String url) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            String message = "Failed to parse JSON response from weather API service";
            log.error("{} for URL: {}", message, url, e);
            throw new ExternalHttpCallException(message);
        } catch (Exception e) {
            String message = "External HTTP call failed due to network or connection issues";
            log.error("{} for URL: {}", message, url, e);
            throw new ExternalHttpCallException(message);
        }
    }

    private String buildUrl(String suffix, String queryParams) {
        return String.format("%s%s?appid=%s&units=metric&lang=en%s",
                baseApiUrl,
                suffix,
                apiKey,
                queryParams);
    }

}
