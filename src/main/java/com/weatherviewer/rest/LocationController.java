package com.weatherviewer.rest;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.service.LocationService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @version 0.0.1
 */
@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID addLocation(@RequestBody @Valid AddLocationDto addLocationDto) {
        log.info("Request: addLocation called with name={} for userId={}",
                addLocationDto.getName(), addLocationDto.getUserId());
        return locationService.add(addLocationDto);
    }

    @GetMapping
    public List<LocationDto> getLocations() {
        log.info("Request: getLocations called");
        return locationService.getLocations();
    }

    @GetMapping("/{id}")
    public LocationDto getLocationById(@PathVariable UUID id) {
        log.info("Request: getLocationById called with id={}", id);
        return locationService.getById(id);
    }

    @GetMapping("/user/{id}")
    public List<LocationDto> getLocationsByUserId(@PathVariable UUID id) {
        log.info("Request: getLocationsByUserId called for userId={}", id);
        return locationService.getByUserId(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocationById(@PathVariable UUID id) {
        log.info("Request: deleteLocationById called with id={}", id);
        locationService.delete(id);
        log.info("Location deleted successfully: id={}", id);
    }

    @DeleteMapping("/user/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocationsByUserId(@PathVariable UUID id) {
        log.info("Request: deleteLocationsByUserId called for userId={}", id);
        locationService.deleteByUserId(id);
        log.info("All locations deleted for userId={}", id);
    }

    @DeleteMapping("/{name}/user/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocationByNameAndUserId(@PathVariable UUID id, @PathVariable @NotBlank String name) {
        log.info("Request: deleteLocationByNameAndUserId called for userId={} and name={}", id, name);
        locationService.deleteByNameAndUserId(name, id);
        log.info("Location deleted successfully: name={} for userId={}", name, id);
    }

    @GetMapping("/exists/name/{name}/user/{userId}")
    public boolean existsByNameAndUserId(@PathVariable String name, @PathVariable UUID userId) {
        log.info("Request: existsByNameAndUserId called with name={} and userId={}", name, userId);
        return locationService.existsByNameAndUserId(name, userId);
    }

    @GetMapping("/exists/coordinates/user/{userId}")
    public boolean existsByCoordinatesAndUserId(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @PathVariable UUID userId) {

        log.info("Request: existsByCoordinatesAndUserId called with latitude={}, longitude={}, userId={}",
                latitude, longitude, userId);
        return locationService.existsByCoordinatesAndUserId(latitude, longitude, userId);
    }

    @PostMapping("/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addToFavorite(@PathVariable UUID id, @RequestParam UUID userId) {
        log.info("Request: addToFavorite called for locationId={} and userId={}", id, userId);
        locationService.addToFavorite(id, userId);
    }

    @DeleteMapping("/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromFavorite(@PathVariable UUID id, @RequestParam UUID userId) {
        log.info("Request: removeFromFavorite called for locationId={} and userId={}", id, userId);
        locationService.removeFromFavorite(id, userId);
    }

    @GetMapping("/user/{userId}/locations")
    public List<LocationDto> getSortedLocationsByUserId(
            @PathVariable UUID userId,
            @RequestParam(required = false, defaultValue = "date")
            @Parameter(description = "Sort options: date, nameAsc, nameDesc, favoriteFirst, favoritesOnly") String sort) {

        log.info("Request: getSortedLocationsByUserId called for userId={}, sort={}", userId, sort);

        return switch (sort.toLowerCase()) {
            case "nameasc" -> locationService.getByUserIdSortedByNameAsc(userId);
            case "namedesc" -> locationService.getByUserIdSortedByNameDesc(userId);
            case "favoritefirst" -> locationService.getByUserIdSortedByFavorite(userId);
            case "favoritesonly" -> locationService.getFavoritesByUserId(userId);
            default -> locationService.getByUserIdSortedByDate(userId);
        };
    }

}
