package com.weatherviewer.controller;

import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.service.WeatherApiService;
import com.weatherviewer.service.helper.UnitConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Thymeleaf controller for the main dashboard: lists the current user's
 * saved locations (in the requested sort order) alongside their current
 * weather, and handles deleting/favoriting locations from that page.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final WeatherApiService weatherApiService;
    private final LocationService locationService;
    private final UnitConverter unitConverter;
    private final ExecutorService weatherFetchExecutor;

    /** Per-location timeout for the dashboard's concurrent weather fetches. */
    @Value("${weather.dashboard.fetch-timeout-ms:4000}")
    private long weatherFetchTimeoutMs;

    /** Locations shown per dashboard page. Not user-adjustable, to keep the weather fan-out per request bounded. */
    @Value("${location.dashboard.page-size:12}")
    private int dashboardPageSize;

    /**
     * Renders one page of the dashboard. Locations are fetched pre-sorted
     * and pre-paged by {@code sort} ({@code nameAsc}, {@code nameDesc},
     * {@code favoriteFirst}, {@code favoritesOnly}, or the default
     * {@code date}) and {@code page} (0-based; page size is fixed via
     * {@code location.dashboard.page-size}), then current weather is
     * fetched for just that page's locations concurrently on a bounded
     * pool ({@link com.weatherviewer.config.AppConfig#weatherFetchExecutor})
     * to keep page load fast without competing with the JVM's shared
     * common {@code ForkJoinPool}.
     * <p>
     * Each fetch is capped at {@code weather.dashboard.fetch-timeout-ms}
     * and failures are isolated per location: a single slow or failing
     * provider call surfaces that one location as unavailable instead of
     * failing the whole dashboard.
     */
    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal SecUser user,
                       @RequestParam(required = false, defaultValue = "date") String sort,
                       @RequestParam(required = false, defaultValue = "0") int page) {
        int safePage = Math.max(page, 0);
        log.info("Home page requested by user '{}', sort={}, page={}", user.getUsername(), sort, safePage);

        Page<LocationDto> locationPage = locationService.getByUserIdSorted(
                user.getId(), sort, PageRequest.of(safePage, dashboardPageSize));
        List<LocationDto> pageLocations = locationPage.getContent();

        if (!pageLocations.isEmpty()) {
            Map<LocationDto, CompletableFuture<WeatherDto>> pendingWeather = pageLocations.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            location -> CompletableFuture
                                    .supplyAsync(() -> weatherApiService.getWeatherByLocation(location),
                                            weatherFetchExecutor)
                                    .orTimeout(weatherFetchTimeoutMs, TimeUnit.MILLISECONDS),
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));

            Map<LocationDto, WeatherDto> locationWeatherMap = new LinkedHashMap<>();
            List<String> unavailableLocationNames = new ArrayList<>();

            for (Map.Entry<LocationDto, CompletableFuture<WeatherDto>> entry : pendingWeather.entrySet()) {
                LocationDto location = entry.getKey();
                try {
                    WeatherDto weather = entry.getValue().join();
                    locationWeatherMap.put(location, unitConverter.toDisplayUnits(weather, user.getUnits()));
                } catch (CompletionException ex) {
                    log.warn("Weather fetch failed for location '{}' (user '{}'): {}",
                            location.getName(), user.getUsername(), ex.getCause() != null
                                    ? ex.getCause().getMessage() : ex.getMessage());
                    unavailableLocationNames.add(location.getName());
                }
            }

            model.addAttribute("locationWeatherMap", locationWeatherMap);
            if (!unavailableLocationNames.isEmpty()) {
                model.addAttribute("errorMessages", unavailableLocationNames.stream()
                        .map(name -> "Weather for \"" + name + "\" is temporarily unavailable")
                        .toList());
            }
            log.info("Weather data prepared for {} of {} locations on page {}",
                    locationWeatherMap.size(), pageLocations.size(), safePage);
        }

        model.addAttribute("login", user.getFullName());
        model.addAttribute("sort", sort);
        model.addAttribute("currentPage", locationPage.getNumber());
        model.addAttribute("totalPages", locationPage.getTotalPages());
        model.addAttribute("totalLocations", locationPage.getTotalElements());
        model.addAttribute("hasPreviousPage", locationPage.hasPrevious());
        model.addAttribute("hasNextPage", locationPage.hasNext());
        model.addAttribute("temperatureSymbol", unitConverter.temperatureSymbol(user.getUnits()));
        model.addAttribute("windSpeedUnit", unitConverter.windSpeedUnit(user.getUnits()));
        return "home";
    }

    /** Deletes a saved location (ownership-checked) and redirects back to the dashboard preserving the current sort/page. */
    @DeleteMapping("/locations/{id}")
    public String deleteLocation(@PathVariable UUID id,
                                 @AuthenticationPrincipal SecUser user,
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam(required = false, defaultValue = "date") String sort,
                                 @RequestParam(required = false, defaultValue = "0") int page) {
        log.info("User '{}' is deleting location with id={}", user.getUsername(), id);
        locationService.deleteByIdAndUserId(id, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Location deleted successfully");
        return "redirect:/?sort=" + sort + "&page=" + page;
    }

    @PostMapping("/locations/{id}/favorite")
    public String addToFavorite(@PathVariable UUID id,
                                @AuthenticationPrincipal SecUser user,
                                RedirectAttributes redirectAttributes,
                                @RequestParam(required = false, defaultValue = "date") String sort,
                                @RequestParam(required = false, defaultValue = "0") int page) {
        log.info("User {} is adding location {} to favorites", user.getUsername(), id);
        locationService.addToFavorite(id, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Location added to favorites");
        return "redirect:/?sort=" + sort + "&page=" + page;
    }

    @DeleteMapping("/locations/{id}/favorite")
    public String removeFromFavorite(@PathVariable UUID id,
                                     @AuthenticationPrincipal SecUser user,
                                     RedirectAttributes redirectAttributes,
                                     @RequestParam(required = false, defaultValue = "date") String sort,
                                     @RequestParam(required = false, defaultValue = "0") int page) {
        log.info("User {} is removing location {} from favorites", user.getUsername(), id);
        locationService.removeFromFavorite(id, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Location removed from favorites");
        return "redirect:/?sort=" + sort + "&page=" + page;
    }

}
