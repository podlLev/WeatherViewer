package com.weatherviewer.service.helper;

import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class WeatherAggregatorHelper {

    public List<WeatherDto> aggregateDailyForecast(List<WeatherDto> hourlyForecasts) {
        Map<LocalDate, List<WeatherDto>> dailyGroups = hourlyForecasts.stream()
                .collect(Collectors.groupingBy(f -> f.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        LinkedHashMap::new, Collectors.toList()));

        return dailyGroups.values().stream()
                .map(this::aggregateWeatherDto)
                .collect(Collectors.toList());
    }

    private WeatherDto aggregateWeatherDto(List<WeatherDto> dailyForecasts) {
        return new WeatherDto()
                .setDate(dailyForecasts.get(0).getDate())
                .setTemperature(calculateAverageTemperature(dailyForecasts))
                .setTemperatureMinimum(calculateMinimumTemperature(dailyForecasts))
                .setTemperatureMaximum(calculateMaximumTemperature(dailyForecasts))
                .setWeatherCondition(calculateMostCommonWeatherCondition(dailyForecasts))
                .setTimeOfDay(TimeOfDay.UNDEFINED);
    }

    private Double calculateAverageTemperature(List<WeatherDto> dailyForecasts) {
        return dailyForecasts.stream()
                .mapToDouble(WeatherDto::getTemperature)
                .average()
                .orElse(Double.NaN);
    }

    private Double calculateMinimumTemperature(List<WeatherDto> dailyForecasts) {
        return dailyForecasts.stream()
                .map(WeatherDto::getTemperatureMinimum)
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(Double.NaN);
    }

    private Double calculateMaximumTemperature(List<WeatherDto> dailyForecasts) {
        return dailyForecasts.stream()
                .map(WeatherDto::getTemperatureMaximum)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(Double.NaN);
    }

    private WeatherCondition calculateMostCommonWeatherCondition(List<WeatherDto> dailyForecasts) {
        return dailyForecasts.stream()
                .map(WeatherDto::getWeatherCondition)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(WeatherCondition.UNDEFINED);
    }

}
