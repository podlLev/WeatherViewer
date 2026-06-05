package com.weatherviewer.service;

import com.weatherviewer.dto.GeoLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.model.Location;

import java.util.List;

public interface WeatherApiService {

    WeatherDto fetchWeatherByLocation(LocationDto locationdto);

    WeatherDto fetchWeatherByLocation(Location location);

    WeatherDto fetchWeatherByCity(String city);

    WeatherDto fetchWeatherByCoordinates(double latitude, double longitude);

    List<WeatherDto> fetchDailyForecastByCity(String city);

    List<WeatherDto> fetchDailyForecastByCoordinates(double latitude, double longitude);

    List<WeatherDto> fetchHourlyForecastByCity(String city);

    List<WeatherDto> fetchHourlyForecastByCoordinates(double latitude, double longitude);

    List<GeoLocationDto> fetchCitiesByName(String city);

}
