package com.weatherviewer.repository;

import com.weatherviewer.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Location}.
 * <p>
 * All queries are scoped by {@code userId} so that one user's saved
 * locations are never visible to or mutated by another. The various
 * {@code findByUserIdOrderBy...} methods back the dashboard's sort options
 * (newest first, favorites first, name A-Z/Z-A).
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    /** Returns all locations saved by the given user, in no particular order. */
    List<Location> findByUserId(UUID userId);

    /** Returns a user's locations, most recently added first. */
    List<Location> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /** Looks up a user's saved location by exact coordinates, if one exists. */
    Location findByLatitudeAndLongitudeAndUserId(Double latitude, Double longitude, UUID userId);

    /** Deletes every location owned by the given user (used when a user is removed). */
    void deleteByUserId(UUID userId);

    /** Deletes a single named location owned by the given user. */
    void deleteByNameAndUserId(String name,UUID userId);

    /** Checks whether the user has already saved a location with this exact name. */
    boolean existsByNameAndUserId(String name, UUID userId);

    /** Checks whether the user has already saved a location at these exact coordinates. */
    boolean existsByLatitudeAndLongitudeAndUserId(Double latitude, Double longitude, UUID userId);

    /** Returns only the user's favorited locations, most recently added first. */
    List<Location> findByUserIdAndFavoriteTrueOrderByCreatedAtDesc(UUID userId);

    /** Returns a user's locations with favorites pinned to the top, newest first within each group. */
    List<Location> findByUserIdOrderByFavoriteDescCreatedAtDesc(UUID userId);

    /** Returns a user's locations sorted alphabetically by name (A-Z). */
    List<Location> findByUserIdOrderByNameAsc(UUID userId);

    /** Returns a user's locations sorted reverse-alphabetically by name (Z-A). */
    List<Location> findByUserIdOrderByNameDesc(UUID userId);

}
