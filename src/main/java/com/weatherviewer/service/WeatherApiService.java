package com.weatherviewer.service;

import com.weatherviewer.dto.GeoLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.model.Location;

import java.util.List;

public interface WeatherApiService {

    WeatherDto getWeatherByLocation(LocationDto locationdto);

    WeatherDto getWeatherByLocation(Location location);

    WeatherDto getWeatherByCity(String city);

    WeatherDto getWeatherByCoordinates(double latitude, double longitude);

    List<WeatherDto> getDailyForecastByCity(String city);

    List<WeatherDto> getDailyForecastByCoordinates(double latitude, double longitude);

    List<WeatherDto> getHourlyForecastByCity(String city);

    List<WeatherDto> getHourlyForecastByCoordinates(double latitude, double longitude);

    List<GeoLocationDto> getCitiesByName(String city);

}
