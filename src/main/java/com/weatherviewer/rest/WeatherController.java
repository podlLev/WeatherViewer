package com.weatherviewer.rest;

import com.weatherviewer.dto.GeoLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.service.WeatherApiService;
import com.weatherviewer.validation.annotation.Latitude;
import com.weatherviewer.validation.annotation.Longitude;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for retrieving current weather data, forecasts, and geocoding information.
 * <p>
 * This controller acts as a bridge to the upstream weather provider (OpenWeatherMap)
 * and offers endpoints for:
 * <ul>
 * <li><b>Current Weather:</b> Fetching actual conditions by saved location, city name, or coordinates.</li>
 * <li><b>Daily Forecasts:</b> Fetching aggregated 5-day daily forecasts.</li>
 * <li><b>Hourly Forecasts:</b> Fetching detailed 5-day forecasts in 3-hour intervals.</li>
 * <li><b>Geocoding:</b> Searching and disambiguating city names to get their exact coordinates.</li>
 * </ul>
 * <p>
 * All endpoints are rate-limited and require valid session authentication.
 *
 * @author Lev Pidlisnyi
 * @version 1.0.0
 * @since 2026
 */
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Weather", description = "Current conditions, forecasts and city geocoding")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Not authenticated - missing or invalid session cookie",
                content = @Content(mediaType = "text/plain",
                        examples = @ExampleObject(value = "Full authentication is required to access this resource"))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters (e.g. out-of-range latitude/longitude, blank city)",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Map.class),
                        examples = @ExampleObject(value = "{\"city\": \"must not be blank\"}"))),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded (30 requests/60s for this API by default)",
                content = @Content(mediaType = "text/plain",
                        examples = @ExampleObject(value = "Rate limit exceeded. Try again later."))),
        @ApiResponse(responseCode = "503", description = "The upstream weather provider (OpenWeatherMap) is unavailable",
                content = @Content(mediaType = "text/plain",
                        examples = @ExampleObject(value = "Weather service unavailable")))
})
public class WeatherController {

    private final WeatherApiService weatherApiService;

    @Operation(
            summary = "Get current weather for a saved location",
            description = "Looks up current weather conditions using the coordinates and name carried on the " +
                    "supplied location payload. Useful when you already have a `LocationDto` (e.g. one of the " +
                    "caller's saved locations) and want its current weather in a single call."
    )
    @ApiResponse(responseCode = "200", description = "Current weather for the location",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WeatherDto.class)))
    @GetMapping("/location")
    public WeatherDto getWeatherByLocation(@RequestBody @Valid LocationDto locationDto) {
        log.info("Request: getWeatherByLocation called for location name={} ", locationDto.getName());
        return weatherApiService.getWeatherByLocation(locationDto);
    }

    @Operation(summary = "Get current weather by city name")
    @ApiResponse(responseCode = "200", description = "Current weather for the city",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WeatherDto.class)))
    @GetMapping("/city")
    public WeatherDto getWeatherByCity(
            @Parameter(description = "City name to search for", example = "Kyiv", required = true)
            @RequestParam @NotBlank String city) {
        log.info("Request: getWeatherByCity called for city={}", city);
        return weatherApiService.getWeatherByCity(city);
    }

    @Operation(summary = "Get current weather by coordinates")
    @ApiResponse(responseCode = "200", description = "Current weather for the coordinates",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WeatherDto.class)))
    @GetMapping("/coord")
    public WeatherDto getWeatherByCoordinates(
            @Parameter(description = "Latitude in decimal degrees", example = "50.4501", required = true)
            @RequestParam @Latitude double latitude,
            @Parameter(description = "Longitude in decimal degrees", example = "30.5234", required = true)
            @RequestParam @Longitude double longitude) {
        log.info("Request: getWeatherByCoordinates called with latitude={}, longitude={}", latitude, longitude);
        return weatherApiService.getWeatherByCoordinates(latitude, longitude);
    }

    @Operation(
            summary = "Get 5-day daily forecast by city name",
            description = "Returns one forecast entry per day, aggregated from the underlying 3-hour forecast data."
    )
    @ApiResponse(responseCode = "200", description = "Daily forecast entries, ordered by date",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = WeatherDto.class))))
    @GetMapping("/daily/city")
    public List<WeatherDto> getDailyForecastByCity(
            @Parameter(description = "City name to search for", example = "Kyiv", required = true)
            @RequestParam @NotBlank String city) {
        log.info("Request: getDailyForecastByCity called for city={}", city);
        return weatherApiService.getDailyForecastByCity(city);
    }

    @Operation(summary = "Get 5-day daily forecast by coordinates")
    @ApiResponse(responseCode = "200", description = "Daily forecast entries, ordered by date",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = WeatherDto.class))))
    @GetMapping("/daily/coord")
    public List<WeatherDto> getDailyForecastByCoordinates(
            @Parameter(description = "Latitude in decimal degrees", example = "50.4501", required = true)
            @RequestParam @Latitude double latitude,
            @Parameter(description = "Longitude in decimal degrees", example = "30.5234", required = true)
            @RequestParam @Longitude double longitude) {
        log.info("Request: getDailyForecastByCoordinates called with latitude={}, longitude={}", latitude, longitude);
        return weatherApiService.getDailyForecastByCoordinates(latitude, longitude);
    }

    @Operation(
            summary = "Get hourly forecast by city name",
            description = "Returns forecast entries in 3-hour steps for the next 5 days."
    )
    @ApiResponse(responseCode = "200", description = "Hourly forecast entries, ordered by date/time",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = WeatherDto.class))))
    @GetMapping("/hourly/city")
    public List<WeatherDto> getHourlyForecastByCity(
            @Parameter(description = "City name to search for", example = "Kyiv", required = true)
            @RequestParam @NotBlank String city) {
        log.info("Request: getHourlyForecastByCity called for city={}", city);
        return weatherApiService.getHourlyForecastByCity(city);
    }

    @Operation(summary = "Get hourly forecast by coordinates")
    @ApiResponse(responseCode = "200", description = "Hourly forecast entries, ordered by date/time",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = WeatherDto.class))))
    @GetMapping("/hourly/coord")
    public List<WeatherDto> getHourlyForecastByCoordinates(
            @Parameter(description = "Latitude in decimal degrees", example = "50.4501", required = true)
            @RequestParam @Latitude double latitude,
            @Parameter(description = "Longitude in decimal degrees", example = "30.5234", required = true)
            @RequestParam @Longitude double longitude) {
        log.info("Request: getHourlyForecastByCoordinates called with latitude={}, longitude={}", latitude, longitude);
        return weatherApiService.getHourlyForecastByCoordinates(latitude, longitude);
    }

    @Operation(
            summary = "Search cities by name",
            description = "Geocodes a free-text city name into a list of candidate locations (name, coordinates, " +
                    "country/state) using the OpenWeatherMap geocoding API. Use this to disambiguate a city name " +
                    "before saving a location or requesting its weather."
    )
    @ApiResponse(responseCode = "200", description = "Matching candidate locations",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = GeoLocationDto.class))))
    @GetMapping("/city-search")
    public List<GeoLocationDto> getCitiesByName(
            @Parameter(description = "City name (or partial name) to search for", example = "Lond", required = true)
            @RequestParam @NotBlank String city) {
        log.info("Request: getCitiesByName called for city={}", city);
        return weatherApiService.getCitiesByName(city);
    }

}
