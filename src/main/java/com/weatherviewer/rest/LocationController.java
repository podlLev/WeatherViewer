package com.weatherviewer.rest;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.exception.LocationValidationException;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.validation.annotation.Latitude;
import com.weatherviewer.validation.annotation.Longitude;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
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
    private final Validator validator;

    @GetMapping("/my")
    public List<LocationDto> getMyLocations(@AuthenticationPrincipal SecUser user) {
        log.info("Request: getMyLocations called for userId={}", user.getId());
        return locationService.getByUserId(user.getId());
    }

    @PostMapping("/my")
    @ResponseStatus(HttpStatus.CREATED)
    public UUID addMyLocation(@RequestBody AddLocationDto addLocationDto,
                              @AuthenticationPrincipal SecUser user,
                              BindingResult bindingResult) {
        addLocationDto.setUserId(user.getId());
        validator.validate(addLocationDto, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new LocationValidationException(bindingResult);
        }

        log.info("Request: addMyLocation called with name={} for userId={}", addLocationDto.getName(), user.getId());
        return locationService.add(addLocationDto);
    }

    @DeleteMapping("/my/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyLocation(@PathVariable UUID id, @AuthenticationPrincipal SecUser user) {
        log.info("Request: deleteMyLocation called for locationId={} by userId={}", id, user.getId());
        locationService.deleteByIdAndUserId(id, user.getId());
    }

    @PostMapping("/my/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addMyLocationToFavorite(@PathVariable UUID id, @AuthenticationPrincipal SecUser user) {
        log.info("Request: addMyLocationToFavorite called for locationId={} by userId={}", id, user.getId());
        locationService.addToFavorite(id, user.getId());
    }

    @DeleteMapping("/my/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMyLocationFromFavorite(@PathVariable UUID id, @AuthenticationPrincipal SecUser user) {
        log.info("Request: removeMyLocationFromFavorite called for locationId={} by userId={}", id, user.getId());
        locationService.removeFromFavorite(id, user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('users:write')")
    public UUID addLocation(@RequestBody @Valid AddLocationDto addLocationDto) {
        log.info("Request: addLocation called with name={} for userId={}",
                addLocationDto.getName(), addLocationDto.getUserId());
        return locationService.add(addLocationDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('users:write')")
    public List<LocationDto> getLocations() {
        log.info("Request: getLocations called");
        return locationService.getLocations();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('users:write')")
    public LocationDto getLocationById(@PathVariable UUID id) {
        log.info("Request: getLocationById called with id={}", id);
        return locationService.getById(id);
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasAuthority('users:write')")
    public List<LocationDto> getLocationsByUserId(@PathVariable UUID id) {
        log.info("Request: getLocationsByUserId called for userId={}", id);
        return locationService.getByUserId(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:write')")
    public void deleteLocationById(@PathVariable UUID id, @RequestParam UUID userId) {
        log.info("Request: deleteLocationById called with id={} for userId={}", id, userId);
        locationService.deleteByIdAndUserId(id, userId);
        log.info("Location deleted successfully: id={} for userId={}", id, userId);
    }

    @DeleteMapping("/user/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:write')")
    public void deleteLocationsByUserId(@PathVariable UUID id) {
        log.info("Request: deleteLocationsByUserId called for userId={}", id);
        locationService.deleteByUserId(id);
        log.info("All locations deleted for userId={}", id);
    }

    @DeleteMapping("/{name}/user/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:write')")
    public void deleteLocationByNameAndUserId(@PathVariable UUID id, @PathVariable @NotBlank String name) {
        log.info("Request: deleteLocationByNameAndUserId called for userId={} and name={}", id, name);
        locationService.deleteByNameAndUserId(name, id);
        log.info("Location deleted successfully: name={} for userId={}", name, id);
    }

    @GetMapping("/exists/name/{name}/user/{userId}")
    @PreAuthorize("hasAuthority('users:write')")
    public boolean existsByNameAndUserId(@PathVariable @NotBlank String name, @PathVariable UUID userId) {
        log.info("Request: existsByNameAndUserId called with name={} and userId={}", name, userId);
        return locationService.existsByNameAndUserId(name, userId);
    }

    @GetMapping("/exists/coordinates/user/{userId}")
    @PreAuthorize("hasAuthority('users:write')")
    public boolean existsByCoordinatesAndUserId(
            @RequestParam @Latitude double latitude,
            @RequestParam @Longitude double longitude,
            @PathVariable UUID userId) {

        log.info("Request: existsByCoordinatesAndUserId called with latitude={}, longitude={}, userId={}",
                latitude, longitude, userId);
        return locationService.existsByCoordinatesAndUserId(latitude, longitude, userId);
    }

    @PostMapping("/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:write')")
    public void addToFavorite(@PathVariable UUID id, @RequestParam UUID userId) {
        log.info("Request: addToFavorite called for locationId={} and userId={}", id, userId);
        locationService.addToFavorite(id, userId);
    }

    @DeleteMapping("/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:write')")
    public void removeFromFavorite(@PathVariable UUID id, @RequestParam UUID userId) {
        log.info("Request: removeFromFavorite called for locationId={} and userId={}", id, userId);
        locationService.removeFromFavorite(id, userId);
    }

    @GetMapping("/user/{userId}/locations")
    @PreAuthorize("hasAuthority('users:write')")
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
