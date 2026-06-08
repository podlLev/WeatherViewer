package com.weatherviewer.controller;

import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.model.User;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.service.UserService;
import com.weatherviewer.service.WeatherApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ForecastController {

    private final WeatherApiService weatherApiService;
    private final LocationService locationService;
    private final UserService userService;

    @GetMapping("/forecast")
    public String getForecast(@RequestParam("lat") double latitude,
                              @RequestParam("lon") double longitude,
                              @AuthenticationPrincipal SecUser user,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (user == null) {
            log.info("Anonymous user tried to access forecast at lat={}, lon={}", latitude, longitude);
            redirectAttributes.addFlashAttribute("errorMessage", "You need to sign in first!");
            String target = "/forecast?lat=" + latitude + "&lon=" + longitude;
            String encodedTarget = URLEncoder.encode(target, StandardCharsets.UTF_8);
            return "redirect:/sign-in?redirect=" + encodedTarget;
        }

        log.info("Fetching forecast for user={} at lat={}, lon={}", user.getUsername(), latitude, longitude);

        LocationDto locationDto = locationService.getByCoordinatesAndUserId(latitude, longitude, user.getId());
        List<WeatherDto> hourlyForecast = weatherApiService.getHourlyForecastByCoordinates(latitude, longitude);
        List<WeatherDto> dailyForecast = weatherApiService.getDailyForecastByCoordinates(latitude, longitude);

        log.info("Forecast retrieved for location={} (user={})", locationDto.getName(), user.getUsername());

        model.addAttribute("locationName", locationDto.getName());
        model.addAttribute("hourlyForecast", hourlyForecast);
        model.addAttribute("dailyForecast", dailyForecast);

        User userEntity = userService.getEntityById(user.getId());
        model.addAttribute("login", userEntity.getFullName());

        return "forecast";
    }

}
