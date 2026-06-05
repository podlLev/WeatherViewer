package com.weatherviewer.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.weatherviewer.client.WeatherApiClient;
import com.weatherviewer.dto.GeoLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.mapper.LocationMapper;
import com.weatherviewer.mapper.WeatherApiMapper;
import com.weatherviewer.model.Location;
import com.weatherviewer.service.WeatherApiService;
import com.weatherviewer.service.helper.WeatherAggregatorHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherApiServiceImpl implements WeatherApiService {

    private final WeatherApiClient weatherApiClient;
    private final WeatherAggregatorHelper weatherAggregatorHelper;
    private final WeatherApiMapper weatherApiMapper;
    private final LocationMapper locationMapper;

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
        JsonNode jsonNode = weatherApiClient.fetchCurrentWeatherByCity(city);
        return weatherApiMapper.toWeatherDto(jsonNode);
    }

    @Override
    public WeatherDto getWeatherByCoordinates(double latitude, double longitude) {
        log.info("Fetching current weather for latitude={}, longitude={}", latitude, longitude);
        JsonNode jsonNode = weatherApiClient.fetchCurrentWeatherByCoordinates(latitude, longitude);
        return weatherApiMapper.toWeatherDto(jsonNode);
    }

    @Override
    public List<WeatherDto> getDailyForecastByCity(String city) {
        log.info("Fetching daily forecast for city: {}", city);
        List<WeatherDto> hourlyForecasts = getHourlyForecastByCity(city);
        return weatherAggregatorHelper.aggregateDailyForecast(hourlyForecasts);
    }

    @Override
    public List<WeatherDto> getDailyForecastByCoordinates(double latitude, double longitude) {
        log.info("Fetching daily forecast for latitude={}, longitude={}", latitude, longitude);
        List<WeatherDto> hourlyForecasts = getHourlyForecastByCoordinates(latitude, longitude);
        return weatherAggregatorHelper.aggregateDailyForecast(hourlyForecasts);
    }

    @Override
    public List<WeatherDto> getHourlyForecastByCity(String city) {
        log.info("Fetching hourly forecast for city: {}", city);
        JsonNode jsonNode = weatherApiClient.fetchForecastByCity(city).withArray("list");
        return mapJsonNodeToList(jsonNode, weatherApiMapper::toWeatherDto);
    }

    @Override
    public List<WeatherDto> getHourlyForecastByCoordinates(double latitude, double longitude) {
        log.info("Fetching hourly forecast for latitude={}, longitude={}", latitude, longitude);
        JsonNode jsonNode = weatherApiClient.fetchForecastByCoordinates(latitude, longitude).withArray("list");
        return mapJsonNodeToList(jsonNode, weatherApiMapper::toWeatherDto);
    }

    @Override
    public List<GeoLocationDto> getCitiesByName(String city) {
        log.info("Fetching geo data for city: {}", city);
        JsonNode jsonNode = weatherApiClient.fetchGeocodingByCity(city);
        return mapJsonNodeToList(jsonNode, weatherApiMapper::toGeoLocationDto);
    }

    private <T> List<T> mapJsonNodeToList(JsonNode arrayNode, Function<JsonNode, T> mapper) {
        return StreamSupport.stream(arrayNode.spliterator(), false)
                .map(mapper)
                .collect(Collectors.toList());
    }

}
