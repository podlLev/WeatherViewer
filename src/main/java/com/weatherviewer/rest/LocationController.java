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
        log.info("Request: addLocation called with name={}", addLocationDto.getName());
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

    @GetMapping("/search")
    public LocationDto getLocationByCoordinates(@RequestParam Double latitude, @RequestParam Double longitude) {
        log.info("Request: getLocationByCoordinates called with latitude={}, longitude={}", latitude, longitude);
        return locationService.getByCoordinates(latitude, longitude);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocationById(@PathVariable UUID id) {
        log.info("Request: deleteLocationById called with id={}", id);
        locationService.delete(id);
        log.info("Location deleted successfully: id={}", id);
    }

    @DeleteMapping("/name/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocationByName(@PathVariable @NotBlank String name) {
        log.info("Request: deleteLocationByName called for name={}", name);
        locationService.deleteByName(name);
        log.info("Locations deleted successfully matching name={}", name);
    }

    @GetMapping("/exists/name/{name}")
    public boolean existsByName(@PathVariable String name) {
        log.info("Request: existsByName called with name={}", name);
        return locationService.existsByName(name);
    }

    @GetMapping("/exists/coordinates")
    public boolean existsByCoordinates(@RequestParam Double latitude, @RequestParam Double longitude) {
        log.info("Request: existsByCoordinates called with latitude={}, longitude={}", latitude, longitude);
        return locationService.existsByCoordinates(latitude, longitude);
    }

    @GetMapping("/sorted")
    public List<LocationDto> getSortedLocations(
            @RequestParam(required = false, defaultValue = "date")
            @Parameter(description = "Sort options: date, nameAsc, nameDesc") String sort) {

        log.info("Request: getSortedLocations called with sort={}", sort);

        return switch (sort.toLowerCase()) {
            case "nameasc" -> locationService.getLocationsSortedByNameAsc();
            case "namedesc" -> locationService.getLocationsSortedByNameDesc();
            default -> locationService.getLocationsSortedByDate();
        };
    }

}
