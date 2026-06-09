package com.weatherviewer.rest;

import com.weatherviewer.dto.GeoLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.service.WeatherApiService;
import com.weatherviewer.validation.annotation.Latitude;
import com.weatherviewer.validation.annotation.Longitude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @version 0.0.1
 */
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

    private final WeatherApiService weatherApiService;

    @GetMapping("/location")
    public WeatherDto getWeatherByLocation(@RequestBody @Valid LocationDto locationDto) {
        log.info("Request: getWeatherByLocation called for location name={} ", locationDto.getName());
        return weatherApiService.getWeatherByLocation(locationDto);
    }

    @GetMapping("/city")
    public WeatherDto getWeatherByCity(@RequestParam @NotBlank String city) {
        log.info("Request: getWeatherByCity called for city={}", city);
        return weatherApiService.getWeatherByCity(city);
    }

    @GetMapping("/coord")
    public WeatherDto getWeatherByCoordinates(
            @RequestParam @Latitude double latitude,
            @RequestParam @Longitude double longitude) {
        log.info("Request: getWeatherByCoordinates called with latitude={}, longitude={}", latitude, longitude);
        return weatherApiService.getWeatherByCoordinates(latitude, longitude);
    }

    @GetMapping("/daily/city")
    public List<WeatherDto> getDailyForecastByCity(@RequestParam @NotBlank String city) {
        log.info("Request: getDailyForecastByCity called for city={}", city);
        return weatherApiService.getDailyForecastByCity(city);
    }

    @GetMapping("/daily/coord")
    public List<WeatherDto> getDailyForecastByCoordinates(
            @RequestParam @Latitude double latitude,
            @RequestParam @Longitude double longitude) {
        log.info("Request: getDailyForecastByCoordinates called with latitude={}, longitude={}", latitude, longitude);
        return weatherApiService.getDailyForecastByCoordinates(latitude, longitude);
    }

    @GetMapping("/hourly/city")
    public List<WeatherDto> getHourlyForecastByCity(@RequestParam @NotBlank String city) {
        log.info("Request: getHourlyForecastByCity called for city={}", city);
        return weatherApiService.getHourlyForecastByCity(city);
    }

    @GetMapping("/hourly/coord")
    public List<WeatherDto> getHourlyForecastByCoordinates(
            @RequestParam @Latitude double latitude,
            @RequestParam @Longitude double longitude) {
        log.info("Request: getHourlyForecastByCoordinates called with latitude={}, longitude={}", latitude, longitude);
        return weatherApiService.getHourlyForecastByCoordinates(latitude, longitude);
    }

    @GetMapping("/city-search")
    public List<GeoLocationDto> getCitiesByName(@RequestParam @NotBlank String city) {
        log.info("Request: getCitiesByName called for city={}", city);
        return weatherApiService.getCitiesByName(city);
    }

}
