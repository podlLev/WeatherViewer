package com.weatherviewer.controller;

import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.service.WeatherApiService;
import com.weatherviewer.validation.annotation.Latitude;
import com.weatherviewer.validation.annotation.Longitude;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ForecastController {

    private final WeatherApiService weatherApiService;
    private final LocationService locationService;

    @GetMapping("/forecast")
    public String getForecast(@RequestParam("lat") @Latitude double latitude,
                              @RequestParam("lon") @Longitude double longitude,
                              @AuthenticationPrincipal SecUser user,
                              Model model) {
        log.info("Fetching forecast for user={} at lat={}, lon={}", user.getUsername(), latitude, longitude);

        String locationName = locationService.getByCoordinatesAndUserId(latitude, longitude, user.getId()).getName();
        List<WeatherDto> hourlyForecast = weatherApiService.getHourlyForecastByCoordinates(latitude, longitude);
        List<WeatherDto> dailyForecast = weatherApiService.getDailyForecastByCoordinates(latitude, longitude);

        log.info("Forecast retrieved for location={} (user={})", locationName, user.getUsername());

        model.addAttribute("locationName", locationName);
        model.addAttribute("hourlyForecast", hourlyForecast);
        model.addAttribute("dailyForecast", dailyForecast);

        model.addAttribute("login", user.getFullName());

        return "forecast";
    }

}
