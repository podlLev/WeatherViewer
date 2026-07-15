package com.weatherviewer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * A city or place saved by a {@link User} to their weather dashboard.
 * <p>
 * A location is identified either by {@link #name} (resolved against the
 * weather provider at lookup time) or by explicit {@link #latitude}/
 * {@link #longitude} coordinates. Locations belong to exactly one user and
 * can be flagged as {@link #favorite} to pin them to the top of the
 * dashboard.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class Location extends BaseEntity {

    /** Display name of the location, e.g. the city name. */
    private String name;

    /** Latitude in decimal degrees; may be {@code null} if only a name was saved. */
    private Double latitude;

    /** Longitude in decimal degrees; may be {@code null} if only a name was saved. */
    private Double longitude;

    /** Whether this location is pinned as a favorite on the owner's dashboard. */
    private boolean favorite;

    /** The user this location belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
