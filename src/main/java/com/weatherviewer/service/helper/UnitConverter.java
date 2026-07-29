package com.weatherviewer.service.helper;

import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.model.enums.UnitSystem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Converts {@link WeatherDto} values from the canonical metric units used
 * by {@code WeatherApiClient} and its cache into a user's preferred
 * display units.
 * <p>
 * Conversion happens here, at the presentation boundary, rather than by
 * requesting a different unit system from the provider per user. That
 * keeps a single shared cache entry per city/coordinate pair usable by
 * every user regardless of their preference, instead of doubling cache
 * entries (and provider requests) for every location.
 */
@Component
public class UnitConverter {

    private static final double CELSIUS_TO_FAHRENHEIT_MULTIPLIER = 9.0 / 5.0;
    private static final double CELSIUS_TO_FAHRENHEIT_OFFSET = 32.0;
    private static final double METERS_PER_SECOND_TO_MPH = 2.2369362921;

    /**
     * Returns {@code weather} unchanged if {@code units} is {@link UnitSystem#METRIC}
     * (the storage/cache unit), or a converted copy if {@link UnitSystem#IMPERIAL}.
     * The original {@code weather} instance is never mutated, since it may
     * be a shared object backed by the cache.
     */
    public WeatherDto toDisplayUnits(WeatherDto weather, UnitSystem units) {
        if (weather == null || units == UnitSystem.METRIC) {
            return weather;
        }

        return new WeatherDto()
                .setWeatherCondition(weather.getWeatherCondition())
                .setTimeOfDay(weather.getTimeOfDay())
                .setDescription(weather.getDescription())
                .setTemperature(toFahrenheit(weather.getTemperature()))
                .setTemperatureFeelsLike(toFahrenheit(weather.getTemperatureFeelsLike()))
                .setTemperatureMinimum(toFahrenheit(weather.getTemperatureMinimum()))
                .setTemperatureMaximum(toFahrenheit(weather.getTemperatureMaximum()))
                .setHumidity(weather.getHumidity())
                .setPressure(weather.getPressure())
                .setWindSpeed(toMph(weather.getWindSpeed()))
                .setWindDirection(weather.getWindDirection())
                .setWindGust(toMph(weather.getWindGust()))
                .setCloudiness(weather.getCloudiness())
                .setDate(weather.getDate())
                .setSunrise(weather.getSunrise())
                .setSunset(weather.getSunset());
    }

    /** Applies {@link #toDisplayUnits(WeatherDto, UnitSystem)} to every entry in a list. */
    public List<WeatherDto> toDisplayUnits(List<WeatherDto> weatherList, UnitSystem units) {
        if (weatherList == null || units == UnitSystem.METRIC) {
            return weatherList;
        }
        return weatherList.stream().map(w -> toDisplayUnits(w, units)).toList();
    }

    /** The temperature unit symbol to show alongside converted values ({@code "°C"} or {@code "°F"}). */
    public String temperatureSymbol(UnitSystem units) {
        return units == UnitSystem.IMPERIAL ? "°F" : "°C";
    }

    /** The wind speed unit label to show alongside converted values ({@code "m/s"} or {@code "mph"}). */
    public String windSpeedUnit(UnitSystem units) {
        return units == UnitSystem.IMPERIAL ? "mph" : "m/s";
    }

    private Double toFahrenheit(Double celsius) {
        return celsius == null ? null : round((celsius * CELSIUS_TO_FAHRENHEIT_MULTIPLIER) + CELSIUS_TO_FAHRENHEIT_OFFSET);
    }

    private Double toMph(Double metersPerSecond) {
        return metersPerSecond == null ? null : round(metersPerSecond * METERS_PER_SECOND_TO_MPH);
    }

    /**
     * Rounds to one decimal place using half-up rounding (e.g. {@code 71.249999...}
     * becomes {@code 71.2}, not a repeating-decimal display artifact of the raw
     * multiplication). Goes through {@link BigDecimal} rather than
     * {@code Math.round(x * 10) / 10.0} to avoid the binary floating-point
     * representation error that approach can introduce at the tenths place.
     */
    private Double round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

}
