package com.weatherviewer.rest;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.exception.FieldError;
import com.weatherviewer.exception.LocationValidationException;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Not authenticated - missing or invalid session cookie",
                content = @Content(mediaType = "text/plain",
                        examples = @ExampleObject(value = "Full authentication is required to access this resource"))),
        @ApiResponse(responseCode = "403", description = "Authenticated but missing the required authority",
                content = @Content(mediaType = "text/plain",
                        examples = @ExampleObject(value = "Access Denied"))),
        @ApiResponse(responseCode = "404", description = "Location or user not found",
                content = @Content(mediaType = "text/plain",
                        examples = @ExampleObject(value = "Location not found: id=3fa85f64-5717-4562-b3fc-2c963f66afa6"))),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded (60 requests/60s for this API by default)",
                content = @Content(mediaType = "text/plain",
                        examples = @ExampleObject(value = "Rate limit exceeded. Try again later.")))
})
public class LocationController {

    private final LocationService locationService;
    private final Validator validator;

    // ---------------------------------------------------------------
    // "My" endpoints - operate on the authenticated caller's own data
    // ---------------------------------------------------------------

    @Operation(
            tags = "Locations - My",
            summary = "List my saved locations",
            description = "Returns every location saved by the currently authenticated user, in insertion order."
    )
    @ApiResponse(responseCode = "200", description = "The caller's saved locations",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = LocationDto.class))))
    @GetMapping("/my")
    public List<LocationDto> getMyLocations(@AuthenticationPrincipal SecUser user) {
        log.info("Request: getMyLocations called for userId={}", user.getId());
        return locationService.getByUserId(user.getId());
    }

    @Operation(
            tags = "Locations - My",
            summary = "Add a location to my saved list",
            description = "Creates a new saved location owned by the currently authenticated user. " +
                    "Any `userId` supplied in the request body is ignored and overwritten with the caller's ID."
    )
    @ApiResponse(responseCode = "201", description = "Location created; returns the new location's ID",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UUID.class)))
    @ApiResponse(responseCode = "422", description = "Validation failed (e.g. blank name, out-of-range coordinates, duplicate location)",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = FieldError.class)),
                    examples = @ExampleObject(value = """
                            [
                              {"field": "name", "message": "Name cannot be blank"},
                              {"field": "latitude", "message": "Latitude must be between -90 and 90"}
                            ]""")))
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

    @Operation(tags = "Locations - My", summary = "Delete one of my saved locations")
    @ApiResponse(responseCode = "204", description = "Location deleted")
    @DeleteMapping("/my/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyLocation(
            @Parameter(description = "ID of the location to delete") @PathVariable UUID id,
            @AuthenticationPrincipal SecUser user) {
        log.info("Request: deleteMyLocation called for locationId={} by userId={}", id, user.getId());
        locationService.deleteByIdAndUserId(id, user.getId());
    }

    @Operation(tags = "Locations - My", summary = "Mark one of my locations as a favorite")
    @ApiResponse(responseCode = "204", description = "Location marked as favorite")
    @PostMapping("/my/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addMyLocationToFavorite(
            @Parameter(description = "ID of the location to favorite") @PathVariable UUID id,
            @AuthenticationPrincipal SecUser user) {
        log.info("Request: addMyLocationToFavorite called for locationId={} by userId={}", id, user.getId());
        locationService.addToFavorite(id, user.getId());
    }

    @Operation(tags = "Locations - My", summary = "Remove one of my locations from favorites")
    @ApiResponse(responseCode = "204", description = "Location removed from favorites")
    @DeleteMapping("/my/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMyLocationFromFavorite(
            @Parameter(description = "ID of the location to unfavorite") @PathVariable UUID id,
            @AuthenticationPrincipal SecUser user) {
        log.info("Request: removeMyLocationFromFavorite called for locationId={} by userId={}", id, user.getId());
        locationService.removeFromFavorite(id, user.getId());
    }

    // ---------------------------------------------------------------
    // Admin endpoints - require the `users:write` authority
    // ---------------------------------------------------------------

    @Operation(tags = "Locations - Admin",
            summary = "Add a location for any user",
            description = "Creates a location for the `userId` specified in the request body. Requires the " +
                    "`users:write` authority."
    )
    @ApiResponse(responseCode = "201", description = "Location created; returns the new location's ID",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UUID.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed (bean validation on the request body)",
            content = @Content(mediaType = "text/plain"))
    @SecurityRequirement(name = "sessionCookieAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('users:write')")
    public UUID addLocation(@RequestBody @Valid AddLocationDto addLocationDto) {
        log.info("Request: addLocation called with name={} for userId={}",
                addLocationDto.getName(), addLocationDto.getUserId());
        return locationService.add(addLocationDto);
    }

    @Operation(tags = "Locations - Admin", summary = "List all locations", description = "Requires the `users:write` authority.")
    @ApiResponse(responseCode = "200", description = "All saved locations across all users",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = LocationDto.class))))
    @GetMapping
    @PreAuthorize("hasAuthority('users:write')")
    public List<LocationDto> getLocations() {
        log.info("Request: getLocations called");
        return locationService.getLocations();
    }

    @Operation(tags = "Locations - Admin", summary = "Get a location by ID", description = "Requires the `users:write` authority.")
    @ApiResponse(responseCode = "200", description = "The requested location",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LocationDto.class)))
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('users:write')")
    public LocationDto getLocationById(@Parameter(description = "Location ID") @PathVariable UUID id) {
        log.info("Request: getLocationById called with id={}", id);
        return locationService.getById(id);
    }

    @Operation(tags = "Locations - Admin", summary = "List all locations for a user", description = "Requires the `users:write` authority.")
    @ApiResponse(responseCode = "200", description = "The user's saved locations",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = LocationDto.class))))
    @GetMapping("/user/{id}")
    @PreAuthorize("hasAuthority('users:write')")
    public List<LocationDto> getLocationsByUserId(@Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("Request: getLocationsByUserId called for userId={}", id);
        return locationService.getByUserId(id);
    }

    @Operation(tags = "Locations - Admin", summary = "Delete a location by ID", description = "Requires the `users:write` authority.")
    @ApiResponse(responseCode = "204", description = "Location deleted")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:write')")
    public void deleteLocationById(
            @Parameter(description = "Location ID") @PathVariable UUID id,
            @Parameter(description = "Owning user's ID", required = true) @RequestParam UUID userId) {
        log.info("Request: deleteLocationById called with id={} for userId={}", id, userId);
        locationService.deleteByIdAndUserId(id, userId);
        log.info("Location deleted successfully: id={} for userId={}", id, userId);
    }

    @Operation(tags = "Locations - Admin", summary = "Delete all locations for a user", description = "Requires the `users:write` authority.")
    @ApiResponse(responseCode = "204", description = "Locations deleted")
    @DeleteMapping("/user/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:write')")
    public void deleteLocationsByUserId(@Parameter(description = "User ID") @PathVariable UUID id) {
        log.info("Request: deleteLocationsByUserId called for userId={}", id);
        locationService.deleteByUserId(id);
        log.info("All locations deleted for userId={}", id);
    }

    @Operation(tags = "Locations - Admin", summary = "Delete a location by name for a user", description = "Requires the `users:write` authority.")
    @ApiResponse(responseCode = "204", description = "Location deleted")
    @DeleteMapping("/{name}/user/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:write')")
    public void deleteLocationByNameAndUserId(
            @Parameter(description = "Owning user's ID") @PathVariable UUID id,
            @Parameter(description = "Exact location name") @PathVariable @NotBlank String name) {
        log.info("Request: deleteLocationByNameAndUserId called for userId={} and name={}", id, name);
        locationService.deleteByNameAndUserId(name, id);
        log.info("Location deleted successfully: name={} for userId={}", name, id);
    }

    @Operation(tags = "Locations - Admin", summary = "Check whether a location name is already used by a user",
            description = "Requires the `users:write` authority.")
    @ApiResponse(responseCode = "200", description = "true if the name is already used by the user, false otherwise",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    @GetMapping("/exists/name/{name}/user/{userId}")
    @PreAuthorize("hasAuthority('users:write')")
    public boolean existsByNameAndUserId(
            @Parameter(description = "Location name to check") @PathVariable @NotBlank String name,
            @Parameter(description = "User ID to check against") @PathVariable UUID userId) {
        log.info("Request: existsByNameAndUserId called with name={} and userId={}", name, userId);
        return locationService.existsByNameAndUserId(name, userId);
    }

    @Operation(tags = "Locations - Admin", summary = "Check whether a coordinate pair is already saved by a user",
            description = "Requires the `users:write` authority.")
    @ApiResponse(responseCode = "200", description = "true if the coordinates are already saved by the user, false otherwise",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    @GetMapping("/exists/coordinates/user/{userId}")
    @PreAuthorize("hasAuthority('users:write')")
    public boolean existsByCoordinatesAndUserId(
            @Parameter(description = "Latitude in decimal degrees", example = "50.4501") @RequestParam @Latitude double latitude,
            @Parameter(description = "Longitude in decimal degrees", example = "30.5234") @RequestParam @Longitude double longitude,
            @Parameter(description = "User ID to check against") @PathVariable UUID userId) {

        log.info("Request: existsByCoordinatesAndUserId called with latitude={}, longitude={}, userId={}",
                latitude, longitude, userId);
        return locationService.existsByCoordinatesAndUserId(latitude, longitude, userId);
    }

    @Operation(tags = "Locations - Admin", summary = "Add a location to a user's favorites", description = "Requires the `users:write` authority.")
    @ApiResponse(responseCode = "204", description = "Location marked as favorite")
    @PostMapping("/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:write')")
    public void addToFavorite(
            @Parameter(description = "Location ID") @PathVariable UUID id,
            @Parameter(description = "Owning user's ID") @RequestParam UUID userId) {
        log.info("Request: addToFavorite called for locationId={} and userId={}", id, userId);
        locationService.addToFavorite(id, userId);
    }

    @Operation(tags = "Locations - Admin", summary = "Remove a location from a user's favorites", description = "Requires the `users:write` authority.")
    @ApiResponse(responseCode = "204", description = "Location removed from favorites")
    @DeleteMapping("/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('users:write')")
    public void removeFromFavorite(
            @Parameter(description = "Location ID") @PathVariable UUID id,
            @Parameter(description = "Owning user's ID") @RequestParam UUID userId) {
        log.info("Request: removeFromFavorite called for locationId={} and userId={}", id, userId);
        locationService.removeFromFavorite(id, userId);
    }

    @Operation(tags = "Locations - Admin",
            summary = "List a user's locations with sorting",
            description = "Requires the `users:write` authority."
    )
    @ApiResponse(responseCode = "200", description = "The user's saved locations, sorted as requested",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = LocationDto.class))))
    @GetMapping("/user/{userId}/locations")
    @PreAuthorize("hasAuthority('users:write')")
    public List<LocationDto> getSortedLocationsByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Sort options: date, nameAsc, nameDesc, favoriteFirst, favoritesOnly")
            @RequestParam(required = false, defaultValue = "date") String sort) {

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
