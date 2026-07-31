package com.weatherviewer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.rest.MapTileController;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders the {@code /map} page: a Leaflet world map with a marker for
 * each of the current user's saved locations, a toggleable OpenWeatherMap
 * overlay (precipitation/clouds/temperature/wind) proxied through
 * {@link MapTileController} so the OpenWeatherMap API key never reaches
 * the browser, and a click-to-add flow that lets the user save a new
 * location by clicking a spot on the map (submitted to the existing
 * {@code /search/add} endpoint).
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MapController {

    private final LocationService locationService;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    @GetMapping("/map")
    public String map(Model model, @AuthenticationPrincipal SecUser user, HttpServletRequest request) {
        List<LocationDto> locations = locationService.getByUserId(user.getId());
        log.info("Map page requested by user '{}', {} saved location(s)", user.getUsername(), locations.size());

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        model.addAttribute("login", user.getFullName());
        model.addAttribute("locations", locations);
        model.addAttribute("mapDataJson", buildMapDataJson(locations, csrfToken));
        return "map";
    }

    private String buildMapDataJson(List<LocationDto> locations, CsrfToken csrfToken) {
        Map<String, Object> layerLabels = new LinkedHashMap<>();
        layerLabels.put("precipitation", messageSource.getMessage("map.layer.precipitation", null, LocaleContextHolder.getLocale()));
        layerLabels.put("clouds", messageSource.getMessage("map.layer.clouds", null, LocaleContextHolder.getLocale()));
        layerLabels.put("temperature", messageSource.getMessage("map.layer.temperature", null, LocaleContextHolder.getLocale()));
        layerLabels.put("wind", messageSource.getMessage("map.layer.wind", null, LocaleContextHolder.getLocale()));

        Map<String, Object> labels = new LinkedHashMap<>();
        labels.put("forecast", messageSource.getMessage("map.popup.forecast", null, LocaleContextHolder.getLocale()));
        labels.put("addLocation", messageSource.getMessage("map.popup.add-location", null, LocaleContextHolder.getLocale()));
        labels.put("locationNamePlaceholder", messageSource.getMessage("map.popup.location-name-placeholder", null, LocaleContextHolder.getLocale()));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("locations", locations);
        data.put("layerLabels", layerLabels);
        data.put("labels", labels);
        data.put("forecastUrl", "/forecast");
        data.put("addLocationUrl", "/search/add");

        data.put("csrfParam", csrfToken.getParameterName());
        data.put("csrfToken", csrfToken.getToken());

        try {
            return objectMapper.writeValueAsString(data).replace("</", "<\\/");
        } catch (Exception e) {
            log.warn("Failed to serialize map data to JSON", e);
            return "{\"locations\":[],\"layerLabels\":{}}";
        }
    }

}
