package com.weatherviewer.controller;

import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.service.WeatherApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherApiService weatherApiService;

    @GetMapping
    public ResponseEntity<WeatherDto> getWeatherByCity(@RequestParam String city) {
        WeatherDto weather = weatherApiService.fetchWeatherByCity(city);
        return ResponseEntity.ok(weather);
    }

}
