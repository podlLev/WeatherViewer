package com.weatherviewer.model.enums;

import com.weatherviewer.exception.notfound.UnitSystemNotFoundException;

/**
 * A user's preferred unit system for displaying weather data.
 * <p>
 * Weather is always fetched from the provider and cached in metric units
 * (see {@code WeatherApiClient}); conversion to the user's preferred
 * system happens at render time via {@code UnitConverter}, so switching a
 * user's preference never invalidates the shared weather cache.
 */
public enum UnitSystem {

    /** Celsius, meters/second. */
    METRIC,
    /** Fahrenheit, miles/hour. */
    IMPERIAL;

    /**
     * Resolves a unit system by name, case-insensitively.
     *
     * @param name the unit system name to look up (e.g. {@code "imperial"})
     * @return the matching {@link UnitSystem}
     * @throws UnitSystemNotFoundException if no unit system matches the given name
     */
    public static UnitSystem getInstance(String name) {
        for (UnitSystem unitSystem : values()) {
            if (unitSystem.name().equalsIgnoreCase(name)) {
                return unitSystem;
            }
        }
        throw new UnitSystemNotFoundException("Unit system not found by name: " + name);
    }

}
