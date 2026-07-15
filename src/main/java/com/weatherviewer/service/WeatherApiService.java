package com.weatherviewer.service;

import com.weatherviewer.dto.GeoLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.model.Location;

import java.util.List;

/**
 * Application-facing weather operations: current conditions, daily and
 * hourly forecasts, and city geocoding. Implementations are responsible for
 * calling the OpenWeatherMap API (typically through a caching layer) and
 * mapping the raw response into this application's DTOs.
 */
public interface WeatherApiService {

    /**
     * Gets current weather for a location, preferring its coordinates when
     * present and falling back to its name otherwise.
     */
    WeatherDto getWeatherByLocation(LocationDto locationdto);

    /** Entity overload of {@link #getWeatherByLocation(LocationDto)}. */
    WeatherDto getWeatherByLocation(Location location);

    /** Gets current weather for a city by name. */
    WeatherDto getWeatherByCity(String city);

    /** Gets current weather for a specific latitude/longitude. */
    WeatherDto getWeatherByCoordinates(double latitude, double longitude);

    /** Gets a 5-day daily forecast (one entry per day) for a city by name. */
    List<WeatherDto> getDailyForecastByCity(String city);

    /** Gets a 5-day daily forecast (one entry per day) for a coordinate pair. */
    List<WeatherDto> getDailyForecastByCoordinates(double latitude, double longitude);

    /** Gets a 5-day forecast in 3-hour steps for a city by name. */
    List<WeatherDto> getHourlyForecastByCity(String city);

    /** Gets a 5-day forecast in 3-hour steps for a coordinate pair. */
    List<WeatherDto> getHourlyForecastByCoordinates(double latitude, double longitude);

    /**
     * Geocodes a free-text city name into candidate locations (name,
     * coordinates, country/state), for disambiguating a search before
     * saving a location.
     */
    List<GeoLocationDto> getCitiesByName(String city);

}
