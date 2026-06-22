package com.weatherviewer.dto.enums;

public enum WeatherCondition {

    THUNDERSTORM,
    DRIZZLE,
    RAIN,
    SNOW,
    ATMOSPHERE,
    CLEAR,
    CLOUDS,
    UNDEFINED;

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
