package com.weatherviewer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.exception.ExternalHttpCallException;
import com.weatherviewer.mapper.WeatherApiMapper;
import com.weatherviewer.service.WeatherApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherApiServiceImpl implements WeatherApiService {

    private final WeatherApiMapper weatherApiMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.base.api.url}")
    private String baseApiUrl;

    @Value("${weather.api.url.suffix}")
    private String weatherApiUrlSuffix;

    @Override
    public WeatherDto fetchWeatherByCity(String city) {
        log.info("Fetching weather for city: {}", city);
        String url = String.format("%s%s?appid=%s&units=metric&lang=en&q=%s",
                baseApiUrl, weatherApiUrlSuffix, apiKey, city);
        return fetchWeather(url);
    }

    private WeatherDto fetchWeather(String url) {
        JsonNode jsonNode = fetchJson(url);
        return weatherApiMapper.toWeatherDto(jsonNode);
    }

    private JsonNode fetchJson(String url) {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        try {
            return objectMapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            String message = "Failed to parse JSON response from weather API";
            log.error(message, e);
            throw new ExternalHttpCallException(message);
        }
    }

}
