package com.weatherviewer.service.integration;

import com.weatherviewer.exception.ExternalHttpCallException;
import com.weatherviewer.rest.MapTileController;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.regex.Pattern;

/**
 * Thin HTTP client for OpenWeatherMap's map-tile endpoints (precipitation,
 * clouds, temperature, and wind overlays for the {@code /map} page), used
 * by {@link MapTileController}.
 * <p>
 * Deliberately kept separate from {@link WeatherApiClient}: tiles are a
 * different traffic shape (a single pan/zoom can request dozens at once),
 * non-critical (a missing tile just doesn't render, unlike a failed
 * weather lookup), and cacheable for a long time — not worth a circuit
 * breaker of its own, so this only gets a lightweight retry plus a cache.
 * <p>
 * Unlike {@link WeatherApiClient}/{@link WeatherApiCache}, retry and
 * caching live on the same method here rather than being split across two
 * classes — safe because both are still applied via an external Spring
 * proxy call (from {@code MapTileController}), so there's no self-invocation
 * concern, and one client/cache method is simple enough not to need the
 * extra separation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherTileClient {

    private static final Pattern APPID_PATTERN = Pattern.compile("(?i)([?&]appid=)[^&]*");

    private final RestClient restClient;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.tile.base.url}")
    private String tileBaseUrl;

    /**
     * Fetches a single PNG tile for the given layer/zoom/coordinates.
     * Cached by {@code mapTileCache} (see {@code application.properties}
     * for its TTL) since the same tile is requested repeatedly across
     * users and across pans/zooms that revisit the same area.
     */
    @Retry(name = "mapTile")
    @Cacheable("mapTileCache")
    public byte[] fetchTile(MapTileLayer layer, int z, int x, int y) {
        String url = UriComponentsBuilder
                .fromUri(URI.create(tileBaseUrl + "/" + layer.getOwmCode() + "/" + z + "/" + x + "/" + y + ".png"))
                .queryParam("appid", apiKey)
                .build()
                .toUriString();

        try {
            byte[] tile = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(byte[].class);
            log.debug("Fetched map tile layer={} z={} x={} y={}", layer, z, x, y);
            return tile;
        } catch (RestClientResponseException e) {
            log.warn("Map tile API returned {} for URL: {}", e.getStatusCode(), maskApiKey(url));
            boolean retryable = e.getStatusCode() == null || !e.getStatusCode().is4xxClientError();
            throw new ExternalHttpCallException("Map tile API error: " + e.getStatusCode(), retryable);
        } catch (Exception e) {
            log.warn("Map tile fetch failed due to network or connection issues for URL: {}", maskApiKey(url));
            throw new ExternalHttpCallException("Map tile fetch failed due to network or connection issues");
        }
    }

    /** Masks the {@code appid} query parameter so the API key never reaches application logs. */
    private static String maskApiKey(String url) {
        return APPID_PATTERN.matcher(url).replaceAll("$1***");
    }

}
