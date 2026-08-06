package com.weatherviewer.rest;

import com.weatherviewer.service.integration.MapTileLayer;
import com.weatherviewer.service.integration.WeatherTileClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Optional;

/**
 * Proxies OpenWeatherMap's map-tile endpoints for the {@code /map} page.
 * <p>
 * Without this, {@code map.html} would have to request tiles directly
 * from OpenWeatherMap in the browser, which means putting the API key
 * (OWM's tile API is authenticated via an {@code appid} query parameter,
 * same as every other OWM endpoint this app calls) directly into a URL
 * visible in the page's network requests. Routing through here instead
 * keeps the key server-side, same as every other OpenWeatherMap call in
 * this application.
 * <p>
 * {@code layer} is resolved against {@link MapTileLayer}'s whitelist
 * before anything is fetched — an unrecognized value is rejected as a bad
 * request rather than passed through to OpenWeatherMap.
 */
@RestController
@RequiredArgsConstructor
public class MapTileController {

    private final WeatherTileClient weatherTileClient;

    @GetMapping(value = "/map/tiles/{layer}/{z}/{x}/{y}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> tile(@PathVariable String layer,
                                       @PathVariable int z,
                                       @PathVariable int x,
                                       @PathVariable int y) {
        Optional<MapTileLayer> resolvedLayer = MapTileLayer.fromRequestValue(layer);
        if (resolvedLayer.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        byte[] tile = weatherTileClient.fetchTile(resolvedLayer.get(), z, x, y);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(tile);
    }

}
