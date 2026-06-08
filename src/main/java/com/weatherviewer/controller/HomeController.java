package com.weatherviewer.controller;

import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.service.WeatherApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final WeatherApiService weatherApiService;
    private final LocationService locationService;

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal SecUser user,
                       @RequestParam(required = false, defaultValue = "date") String sort) {
        log.info("Home page requested by user '{}', sort={}", user.getUsername(), sort);

        List<LocationDto> userLocations = switch (sort.toLowerCase()) {
            case "nameasc" -> locationService.getByUserIdSortedByNameAsc(user.getId());
            case "namedesc" -> locationService.getByUserIdSortedByNameDesc(user.getId());
            case "favoritefirst" -> locationService.getByUserIdSortedByFavorite(user.getId());
            case "favoritesonly" -> locationService.getFavoritesByUserId(user.getId());
            default -> locationService.getByUserIdSortedByDate(user.getId());
        };

        if (!userLocations.isEmpty()) {
            Map<LocationDto, WeatherDto> locationWeatherMap = userLocations.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            weatherApiService::getWeatherByLocation,
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));
            model.addAttribute("locationWeatherMap", locationWeatherMap);
            log.info("Weather data prepared for {} locations", locationWeatherMap.size());
        }

        model.addAttribute("login", user.getFullName());
        model.addAttribute("sort", sort);
        return "home";
    }

    @DeleteMapping("/locations/{id}")
    public String deleteLocation(@PathVariable UUID id,
                                 @AuthenticationPrincipal SecUser user,
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam(required = false, defaultValue = "date") String sort) {
        log.info("User '{}' is deleting location with id={}", user.getUsername(), id);
        locationService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Location deleted successfully");
        return "redirect:/?sort=" + sort;
    }

    @PostMapping("/locations/{id}/favorite")
    public String addToFavorite(@PathVariable UUID id,
                                @AuthenticationPrincipal SecUser user,
                                RedirectAttributes redirectAttributes,
                                @RequestParam(required = false, defaultValue = "date") String sort) {
        log.info("User {} is adding location {} to favorites", user.getUsername(), id);
        locationService.addToFavorite(id, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Location added to favorites");
        return "redirect:/?sort=" + sort;
    }

    @DeleteMapping("/locations/{id}/favorite")
    public String removeFromFavorite(@PathVariable UUID id,
                                     @AuthenticationPrincipal SecUser user,
                                     RedirectAttributes redirectAttributes,
                                     @RequestParam(required = false, defaultValue = "date") String sort) {
        log.info("User {} is removing location {} from favorites", user.getUsername(), id);
        locationService.removeFromFavorite(id, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Location removed from favorites");
        return "redirect:/?sort=" + sort;
    }

}
