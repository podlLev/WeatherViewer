package com.weatherviewer.service;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.model.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Business operations for creating, reading, and managing a user's saved
 * {@link Location}s — including favoriting and the various dashboard sort
 * orders (by date added, by name, or favorites-first).
 */
public interface LocationService {

    /**
     * Saves a new location for its owning user.
     *
     * @return the ID of the newly created location
     */
    UUID add(AddLocationDto addLocationDto);

    /**
     * Returns saved locations across all users a page at a time (admin
     * use). Unbounded {@code findAll()} doesn't scale once the location
     * count grows past a trivial size, so this is the only way callers can
     * list every location system-wide.
     */
    Page<LocationDto> getLocations(Pageable pageable);

    /**
     * Fetches the raw {@link Location} entity by ID.
     *
     * @throws com.weatherviewer.exception.notfound.LocationNotFoundException if no location has that ID
     */
    Location getEntityById(UUID id);

    /**
     * Fetches a location by ID as a DTO.
     *
     * @throws com.weatherviewer.exception.notfound.LocationNotFoundException if no location has that ID
     */
    LocationDto getById(UUID id);

    /** Returns all locations saved by the given user, unsorted. */
    List<LocationDto> getByUserId(UUID userId);

    /**
     * Counts how many locations the given user currently has saved.
     * Backs {@link com.weatherviewer.validation.annotation.LocationLimit},
     * which rejects new locations once a user hits the configured cap
     * ({@code location.max-per-user}) — without this, a single user could
     * save an unbounded number of locations and force the dashboard to
     * fan out an unbounded number of weather calls on every page load.
     */
    long countByUserId(UUID userId);

    /**
     * Returns one page of a user's saved locations in the given dashboard
     * sort order ({@code date}, {@code nameAsc}, {@code nameDesc},
     * {@code favoriteFirst}, or {@code favoritesOnly} — unrecognized/blank
     * values fall back to {@code date}). Backs the paginated home
     * dashboard, so a user with many saved locations only loads (and
     * fetches weather for) one page's worth per request instead of
     * everything at once.
     */
    Page<LocationDto> getByUserIdSorted(UUID userId, String sort, Pageable pageable);

    /** Looks up a user's saved location by its exact coordinates. */
    LocationDto getByCoordinatesAndUserId(Double latitude, Double longitude, UUID userId);

    /** Deletes a location, verifying it belongs to the given user first. */
    void deleteByIdAndUserId(UUID id, UUID userId);

    /** Deletes every location owned by the given user (used when a user account is deleted). */
    void deleteByUserId(UUID userId);

    /** Deletes a single named location owned by the given user. */
    void deleteByNameAndUserId(String name, UUID userId);

    /** Checks whether the user has already saved a location with this exact name. */
    boolean existsByNameAndUserId(String name, UUID userId);

    /** Checks whether the user has already saved a location at these exact coordinates. */
    boolean existsByCoordinatesAndUserId(Double latitude, Double longitude, UUID userId);

    /** Marks a location as a favorite for its owner. */
    void addToFavorite(UUID locationId, UUID userId);

    /** Unmarks a location as a favorite for its owner. */
    void removeFromFavorite(UUID locationId, UUID userId);

    /** Returns only the user's favorited locations, most recently added first. */
    List<LocationDto> getFavoritesByUserId(UUID userId);

    /** Returns a user's locations with favorites pinned to the top. */
    List<LocationDto> getByUserIdSortedByFavorite(UUID userId);

    /** Returns a user's locations sorted by date added, newest first. */
    List<LocationDto> getByUserIdSortedByDate(UUID userId);

    /** Returns a user's locations sorted alphabetically by name (A-Z). */
    List<LocationDto> getByUserIdSortedByNameAsc(UUID userId);

    /** Returns a user's locations sorted reverse-alphabetically by name (Z-A). */
    List<LocationDto> getByUserIdSortedByNameDesc(UUID userId);

}
