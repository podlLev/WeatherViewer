package com.weatherviewer.dto.enums;

/**
 * Broad weather condition category, mapped from OpenWeatherMap's numeric
 * condition codes. Used to select the right weather icon and to group
 * related conditions (e.g. all rain sub-codes) under one label.
 */
public enum WeatherCondition {

    THUNDERSTORM,
    DRIZZLE,
    RAIN,
    SNOW,
    ATMOSPHERE,
    CLEAR,
    CLOUDS,
    UNDEFINED;

    /**
     * Maps an OpenWeatherMap condition code to its broad category.
     * <p>
     * Ranges follow OpenWeatherMap's own grouping: 2xx thunderstorm, 3xx
     * drizzle, 5xx rain, 6xx snow, 7xx atmosphere (fog/mist/etc.), 800 clear,
     * 801–899 clouds.
     *
     * @param code the provider's condition code; may be {@code null}
     * @return the matching {@link WeatherCondition}, or {@link #UNDEFINED}
     *         if {@code code} is {@code null} or outside all known ranges
     */
    public static WeatherCondition getWeatherConditionForCode(Integer code) {
        if (code == null) return UNDEFINED;

        if (code >= 200 && code < 300) return THUNDERSTORM;
        if (code >= 300 && code < 400) return DRIZZLE;
        if (code >= 500 && code < 600) return RAIN;
        if (code >= 600 && code < 700) return SNOW;
        if (code >= 700 && code < 800) return ATMOSPHERE;
        if (code == 800) return CLEAR;
        if (code > 800 && code < 900)  return CLOUDS;

        return UNDEFINED;
    }

}
