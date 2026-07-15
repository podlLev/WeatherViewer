package com.weatherviewer.service.helper;

import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregates OpenWeatherMap's native 3-hour forecast entries into one
 * summary entry per calendar day, since the provider does not expose a
 * true daily forecast on the free tier.
 */
@Component
public class WeatherAggregatorHelper {

    /**
     * Groups 3-hour forecast entries by calendar day (in the server's
     * default time zone) and collapses each day's entries into a single
     * summary {@link WeatherDto} via {@link #aggregateWeatherDto}.
     *
     * @param hourlyForecasts 3-hour forecast entries, expected in chronological order
     * @return one aggregated entry per day, in the same day order as the input
     */
    public List<WeatherDto> aggregateDailyForecast(List<WeatherDto> hourlyForecasts) {
        Map<LocalDate, List<WeatherDto>> dailyGroups = hourlyForecasts.stream()
                .collect(Collectors.groupingBy(f -> f.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        LinkedHashMap::new, Collectors.toList()));

        return dailyGroups.values().stream()
                .map(this::aggregateWeatherDto)
                .collect(Collectors.toList());
    }

    /**
     * Builds one day's summary entry: the date of its first reading,
     * average/min/max temperature across the day, and the day's most
     * frequent weather condition. {@code timeOfDay} is left
     * {@link TimeOfDay#UNDEFINED} since a whole day spans both.
     */
    private WeatherDto aggregateWeatherDto(List<WeatherDto> dailyForecasts) {
        return new WeatherDto()
                .setDate(dailyForecasts.get(0).getDate())
                .setTemperature(calculateAverageTemperature(dailyForecasts))
                .setTemperatureMinimum(calculateMinimumTemperature(dailyForecasts))
                .setTemperatureMaximum(calculateMaximumTemperature(dailyForecasts))
                .setWeatherCondition(calculateMostCommonWeatherCondition(dailyForecasts))
                .setTimeOfDay(TimeOfDay.UNDEFINED);
    }

    /** Mean of the day's 3-hour temperature readings; {@code NaN} if the list is empty. */
    private Double calculateAverageTemperature(List<WeatherDto> dailyForecasts) {
        return dailyForecasts.stream()
                .mapToDouble(WeatherDto::getTemperature)
                .average()
                .orElse(Double.NaN);
    }

    /** Lowest reported minimum temperature across the day's 3-hour readings. */
    private Double calculateMinimumTemperature(List<WeatherDto> dailyForecasts) {
        return dailyForecasts.stream()
                .map(WeatherDto::getTemperatureMinimum)
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(Double.NaN);
    }

    /** Highest reported maximum temperature across the day's 3-hour readings. */
    private Double calculateMaximumTemperature(List<WeatherDto> dailyForecasts) {
        return dailyForecasts.stream()
                .map(WeatherDto::getTemperatureMaximum)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(Double.NaN);
    }

    /** Weather condition that occurs most often among the day's 3-hour readings. */
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
