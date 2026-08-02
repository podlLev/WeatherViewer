package com.weatherviewer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.WeatherCondition;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.service.WeatherApiService;
import com.weatherviewer.service.helper.UnitConverter;
import com.weatherviewer.validation.annotation.Latitude;
import com.weatherviewer.validation.annotation.Longitude;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thymeleaf controller for the hourly/daily forecast page of one of the
 * current user's saved locations.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ForecastController {

    private final WeatherApiService weatherApiService;
    private final LocationService locationService;
    private final UnitConverter unitConverter;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    /**
     * Renders the forecast page for the saved location at the given
     * coordinates, owned by the authenticated user.
     *
     * @throws com.weatherviewer.exception.notfound.LocationNotFoundException if the user has no saved location there
     */
    @GetMapping("/forecast")
    public String getForecast(@RequestParam("lat") @Latitude double latitude,
                              @RequestParam("lon") @Longitude double longitude,
                              @AuthenticationPrincipal SecUser user,
                              Model model) {
        log.info("Fetching forecast for user={} at lat={}, lon={}", user.getUsername(), latitude, longitude);

        String locationName = locationService.getByCoordinatesAndUserId(latitude, longitude, user.getId()).getName();
        List<WeatherDto> hourlyForecast = unitConverter.toDisplayUnits(
                weatherApiService.getHourlyForecastByCoordinates(latitude, longitude), user.getUnits());
        List<WeatherDto> dailyForecast = unitConverter.toDisplayUnits(
                weatherApiService.getDailyForecastByCoordinates(latitude, longitude), user.getUnits());

        log.info("Forecast retrieved for location={} (user={})", locationName, user.getUsername());

        model.addAttribute("latitude", latitude);
        model.addAttribute("longitude", longitude);
        model.addAttribute("locationName", locationName);
        model.addAttribute("hourlyForecast", hourlyForecast);
        model.addAttribute("dailyForecast", dailyForecast);

        model.addAttribute("login", user.getFullName());
        model.addAttribute("temperatureSymbol", unitConverter.temperatureSymbol(user.getUnits()));
        model.addAttribute("windSpeedUnit", unitConverter.windSpeedUnit(user.getUnits()));
        model.addAttribute("conditionLabelsJson", buildConditionLabelsJson());

        return "forecast";
    }

    /**
     * Maps every {@link WeatherCondition} to its localized {@code weather-condition.*}
     * label, serialized as JSON, so {@code live-forecast.js} can translate the raw
     * enum values pushed over the socket without duplicating
     * {@code messages.properties} in JavaScript.
     */
    private String buildConditionLabelsJson() {
        Map<String, String> labels = new LinkedHashMap<>();
        for (WeatherCondition condition : WeatherCondition.values()) {
            labels.put(condition.name(),
                    messageSource.getMessage("weather-condition." + condition.name(), null, LocaleContextHolder.getLocale()));
        }

        try {
            return objectMapper.writeValueAsString(labels).replace("</", "<\\/");
        } catch (Exception e) {
            log.warn("Failed to serialize weather condition labels to JSON", e);
            return "{}";
        }
    }

}
