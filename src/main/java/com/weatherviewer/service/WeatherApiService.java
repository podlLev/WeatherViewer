package com.weatherviewer.service;

import com.weatherviewer.dto.WeatherDto;

public interface WeatherApiService {

    WeatherDto fetchWeatherByCity(String city);

}
