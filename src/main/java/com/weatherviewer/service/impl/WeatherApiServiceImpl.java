package com.weatherviewer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.dto.GeoLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import com.weatherviewer.exception.ExternalHttpCallException;
import com.weatherviewer.mapper.LocationMapper;
import com.weatherviewer.mapper.WeatherApiMapper;
import com.weatherviewer.model.Location;
import com.weatherviewer.service.WeatherApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherApiServiceImpl implements WeatherApiService {

    private final WeatherApiMapper weatherApiMapper;
    private final LocationMapper locationMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${weather.api.key}")
    private String API_KEY;

    @Value("${weather.base.api.url}")
    private String BASE_API_URL;

    @Value("${weather.api.url.suffix}")
    private String WEATHER_API_URL_SUFFIX;

    @Value("${forecast.api.url.suffix}")
    private String FORECAST_API_URL_SUFFIX;

    @Value("${geo.api.url.suffix}")
    private String GEOCODING_API_URL_SUFFIX;

    @Override
    public WeatherDto getWeatherByLocation(LocationDto locationdto) {
        Location location = locationMapper.fromDto(locationdto);
        return getWeatherByLocation(location);
    }

    @Override
    public WeatherDto getWeatherByLocation(Location location) {
        if (location.getLatitude() != null && location.getLongitude() != null) {
            return getWeatherByCoordinates(location.getLatitude(), location.getLongitude());
        }
        return getWeatherByCity(location.getName());
    }

    @Override
    public WeatherDto getWeatherByCity(String city) {
        log.info("Fetching current weather for city: {}", city);
        String url = buildCityUrl(WEATHER_API_URL_SUFFIX, city);
        return fetchWeather(url);
    }

    private String buildCityUrl(String suffix, String city) {
        return buildUrl(suffix, "&q=" + city);
    }

    private String buildUrl(String suffix, String queryParams) {
        return String.format("%s%s?appid=%s&units=metric&lang=en%s",
                BASE_API_URL,
                suffix,
                API_KEY,
                queryParams);
    }

    private WeatherDto fetchWeather(String url) {
        JsonNode jsonNode = getJsonNodeFromExternalSource(url);
        return weatherApiMapper.toWeatherDto(jsonNode);
    }

    private JsonNode getJsonNodeFromExternalSource(String url) {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return getJson(response);
    }

    private JsonNode getJson(ResponseEntity<String> response) {
        try {
            return objectMapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            String message = "Failed to parse JSON response";
            log.error(message, e);
            throw new ExternalHttpCallException(message);
        }
    }

    @Override
    public WeatherDto getWeatherByCoordinates(double latitude, double longitude) {
        log.info("Fetching current weather for latitude={}, longitude={}", latitude, longitude);
        String url = buildCoordinatesUrl(WEATHER_API_URL_SUFFIX, latitude, longitude);
        return fetchWeather(url);
    }

    private String buildCoordinatesUrl(String suffix, double latitude, double longitude) {
        return buildUrl(suffix, String.format("&lat=%f&lon=%f", latitude, longitude));
    }

    @Override
    public List<WeatherDto> getDailyForecastByCity(String city) {
        log.info("Fetching daily forecast for city: {}", city);
        String url = buildCityUrl(FORECAST_API_URL_SUFFIX, city);
        List<WeatherDto> hourlyForecasts = fetchForecast(url);
        return aggregateDailyForecast(hourlyForecasts);
    }

    private List<WeatherDto> fetchForecast(String url) {
        JsonNode jsonNode = getJsonNodeFromExternalSource(url).withArray("list");
        return mapJsonNodeToList(jsonNode, weatherApiMapper::toWeatherDto);
    }

    private <T> List<T> mapJsonNodeToList(JsonNode arrayNode, Function<JsonNode, T> mapper) {
        return StreamSupport.stream(arrayNode.spliterator(), false)
                .map(mapper)
                .collect(Collectors.toList());
    }

    private List<WeatherDto> aggregateDailyForecast(List<WeatherDto> hourlyForecasts) {
        Map<LocalDate, List<WeatherDto>> dailyGroups = hourlyForecasts.stream()
                .collect(Collectors.groupingBy(f -> f.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        LinkedHashMap::new, Collectors.toList()));

        return dailyGroups.values().stream()
                .map(this::aggregateWeatherDto)
                .collect(Collectors.toList());
    }

    private WeatherDto aggregateWeatherDto(List<WeatherDto> dailyForecasts) {
        return new WeatherDto()
                .setDate(dailyForecasts.get(0).getDate())
                .setTemperature(calculateAverageTemperature(dailyForecasts))
                .setTemperatureMinimum(calculateMinimumTemperature(dailyForecasts))
                .setTemperatureMaximum(calculateMaximumTemperature(dailyForecasts))
                .setWeatherCondition(calculateMostCommonWeatherCondition(dailyForecasts))
                .setTimeOfDay(TimeOfDay.UNDEFINED);
    }

    private Double calculateAverageTemperature(List<WeatherDto> dailyForecasts) {
        return dailyForecasts.stream()
                .mapToDouble(WeatherDto::getTemperature)
                .average()
                .orElse(Double.NaN);
    }

    private Double calculateMinimumTemperature(List<WeatherDto> dailyForecasts) {
        return dailyForecasts.stream()
                .map(WeatherDto::getTemperatureMinimum)
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(Double.NaN);
    }

    private Double calculateMaximumTemperature(List<WeatherDto> dailyForecasts) {
        return dailyForecasts.stream()
                .map(WeatherDto::getTemperatureMaximum)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(Double.NaN);
    }

    private WeatherCondition calculateMostCommonWeatherCondition(List<WeatherDto> dailyForecasts) {
        return dailyForecasts.stream()
                .map(WeatherDto::getWeatherCondition)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(WeatherCondition.UNDEFINED);
    }

    @Override
    public List<WeatherDto> getDailyForecastByCoordinates(double latitude, double longitude) {
        log.info("Fetching daily forecast for latitude={}, longitude={}", latitude, longitude);
        String url = buildCoordinatesUrl(FORECAST_API_URL_SUFFIX, latitude, longitude);
        List<WeatherDto> hourlyForecasts = fetchForecast(url);
        return aggregateDailyForecast(hourlyForecasts);
    }

    @Override
    public List<WeatherDto> getHourlyForecastByCity(String city) {
        log.info("Fetching hourly forecast for city: {}", city);
        String url = buildCityUrl(FORECAST_API_URL_SUFFIX, city);
        return fetchForecast(url);
    }

    @Override
    public List<WeatherDto> getHourlyForecastByCoordinates(double latitude, double longitude) {
        log.info("Fetching hourly forecast for latitude={}, longitude={}", latitude, longitude);
        String url = buildCoordinatesUrl(FORECAST_API_URL_SUFFIX, latitude, longitude);
        return fetchForecast(url);
    }

    @Override
    public List<GeoLocationDto> getCitiesByName(String city) {
        log.info("Fetching geo data for city: {}", city);
        String url = buildUrl(GEOCODING_API_URL_SUFFIX, "&q=" + city + "&limit=10");
        return fetchGeo(url);
    }

    private List<GeoLocationDto> fetchGeo(String url) {
        JsonNode jsonNode = getJsonNodeFromExternalSource(url);
        return mapJsonNodeToList(jsonNode, weatherApiMapper::toGeoLocationDto);
    }

}
