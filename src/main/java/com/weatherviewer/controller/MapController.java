package com.weatherviewer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.rest.MapTileController;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders the {@code /map} page: a Leaflet world map with a marker for
 * each of the current user's saved locations, and a toggleable
 * OpenWeatherMap overlay (precipitation/clouds/temperature/wind) proxied
 * through {@link MapTileController} so the OpenWeatherMap API key never
 * reaches the browser.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MapController {

    private final LocationService locationService;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    @GetMapping("/map")
    public String map(Model model, @AuthenticationPrincipal SecUser user) {
        List<LocationDto> locations = locationService.getByUserId(user.getId());
        log.info("Map page requested by user '{}', {} saved location(s)", user.getUsername(), locations.size());

        model.addAttribute("login", user.getFullName());
        model.addAttribute("locations", locations);
        model.addAttribute("mapDataJson", buildMapDataJson(locations));
        return "map";
    }

    /**
     * Serializes the data {@code map-init.js} needs (saved locations plus
     * translated overlay labels) into a single JSON string, built and
     * escaped entirely server-side rather than via Thymeleaf's
     * JavaScript-inlining ({@code /*[[...]]*}{@code /}) syntax. That syntax
     * relies on comment markers which are only ever stripped for elements
     * Thymeleaf recognizes as JavaScript; since {@code map-data} is
     * {@code type="application/json"} (deliberately, so it's inert data
     * rather than a script the CSP {@code script-src} directive would need
     * to allow), nothing guarantees those markers get removed, and raw
     * {@code /* ... *}{@code /} comments aren't valid JSON regardless. This
     * produces plain, comment-free JSON up front instead.
     */
    private String buildMapDataJson(List<LocationDto> locations) {
        Map<String, Object> layerLabels = new LinkedHashMap<>();
        layerLabels.put("precipitation", messageSource.getMessage("map.layer.precipitation", null, LocaleContextHolder.getLocale()));
        layerLabels.put("clouds", messageSource.getMessage("map.layer.clouds", null, LocaleContextHolder.getLocale()));
        layerLabels.put("temperature", messageSource.getMessage("map.layer.temperature", null, LocaleContextHolder.getLocale()));
        layerLabels.put("wind", messageSource.getMessage("map.layer.wind", null, LocaleContextHolder.getLocale()));

        Map<String, Object> labels = new LinkedHashMap<>();
        labels.put("forecast", messageSource.getMessage("map.popup.forecast", null, LocaleContextHolder.getLocale()));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("locations", locations);
        data.put("layerLabels", layerLabels);
        data.put("labels", labels);
        data.put("forecastUrl", "/forecast");

        try {
            return objectMapper.writeValueAsString(data).replace("</", "<\\/");
        } catch (Exception e) {
            log.warn("Failed to serialize map data to JSON", e);
            return "{\"locations\":[],\"layerLabels\":{}}";
        }
    }

}
