package com.weatherviewer.service.integration;

import com.weatherviewer.rest.MapTileController;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * Whitelist of OpenWeatherMap map-tile overlays exposed via
 * {@link MapTileController} for the
 * {@code /map} page.
 * <p>
 * The enum name (lowercased) is what {@code map.html} requests in the tile
 * URL path; {@link #getOwmCode()} is OpenWeatherMap's own layer identifier,
 * used only when building the upstream request. Resolving through this
 * whitelist — rather than passing whatever path segment the client sent
 * straight through to OpenWeatherMap — keeps the proxy from being usable
 * to reach arbitrary OpenWeatherMap tile endpoints.
 */
@Getter
public enum MapTileLayer {

    PRECIPITATION("precipitation_new"),
    CLOUDS("clouds_new"),
    TEMPERATURE("temp_new"),
    WIND("wind_new");

    private final String owmCode;

    MapTileLayer(String owmCode) {
        this.owmCode = owmCode;
    }

    /** Resolves a request-path layer segment (case-insensitive) to a whitelisted layer, or empty if it doesn't match one. */
    public static Optional<MapTileLayer> fromRequestValue(String value) {
        return Arrays.stream(values())
                .filter(layer -> layer.name().equalsIgnoreCase(value))
                .findFirst();
    }

}
