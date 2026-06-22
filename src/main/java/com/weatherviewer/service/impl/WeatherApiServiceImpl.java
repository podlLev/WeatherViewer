package com.weatherviewer.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.weatherviewer.exception.ExternalHttpCallException;
import com.weatherviewer.service.integration.WeatherApiCache;
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
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherApiServiceImpl implements WeatherApiService {

    private final WeatherApiCache weatherApiCache;
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
        return fetchWeather(() -> weatherApiCache.fetchCurrentWeatherByCity(city));
    }

    private WeatherDto fetchWeather(Supplier<JsonNode> fetcher) {
        return weatherApiMapper.toWeatherDto(fetcher.get());
    }

    @Override
    public WeatherDto getWeatherByCoordinates(double latitude, double longitude) {
        log.info("Fetching current weather for latitude={}, longitude={}", latitude, longitude);
        return fetchWeather(() -> weatherApiCache.fetchCurrentWeatherByCoordinates(latitude, longitude));
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
        return fetchForecastList(() -> weatherApiCache.fetchForecastByCity(city));
    }

    private List<WeatherDto> fetchForecastList(Supplier<JsonNode> fetcher) {
        JsonNode listNode = fetcher.get().path("list");
        if (listNode.isMissingNode() || !listNode.isArray()) {
            throw new ExternalHttpCallException("Missing 'list' field in forecast response");
        }
        return mapJsonNodeToList(listNode, weatherApiMapper::toWeatherDto);
    }

    private <T> List<T> mapJsonNodeToList(JsonNode arrayNode, Function<JsonNode, T> mapper) {
        return StreamSupport.stream(arrayNode.spliterator(), false)
                .map(mapper)
                .toList();
    }

    @Override
    public List<WeatherDto> getHourlyForecastByCoordinates(double latitude, double longitude) {
        log.info("Fetching hourly forecast for latitude={}, longitude={}", latitude, longitude);
        return fetchForecastList(() -> weatherApiCache.fetchForecastByCoordinates(latitude, longitude));
    }

    @Override
    public List<GeoLocationDto> getCitiesByName(String city) {
        log.info("Fetching geo data for city: {}", city);
        JsonNode jsonNode = weatherApiCache.fetchGeocodingByCity(city);
        if (!jsonNode.isArray()) {
            throw new ExternalHttpCallException("Unexpected geocoding response format");
        }
        return mapJsonNodeToList(jsonNode, weatherApiMapper::toGeoLocationDto);
    }

}
