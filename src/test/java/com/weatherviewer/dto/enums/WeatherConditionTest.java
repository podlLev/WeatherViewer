package com.weatherviewer.dto.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherConditionTest {

    @Test
    void code_2xx_returnsThunderstorm() {
        assertThat(WeatherCondition.getWeatherConditionForCode(200)).isEqualTo(WeatherCondition.THUNDERSTORM);
        assertThat(WeatherCondition.getWeatherConditionForCode(299)).isEqualTo(WeatherCondition.THUNDERSTORM);
    }

    @Test
    void code_3xx_returnsDrizzle() {
        assertThat(WeatherCondition.getWeatherConditionForCode(300)).isEqualTo(WeatherCondition.DRIZZLE);
        assertThat(WeatherCondition.getWeatherConditionForCode(321)).isEqualTo(WeatherCondition.DRIZZLE);
    }

    @Test
    void code_5xx_returnsRain() {
        assertThat(WeatherCondition.getWeatherConditionForCode(500)).isEqualTo(WeatherCondition.RAIN);
        assertThat(WeatherCondition.getWeatherConditionForCode(531)).isEqualTo(WeatherCondition.RAIN);
    }

    @Test
    void code_6xx_returnsSnow() {
        assertThat(WeatherCondition.getWeatherConditionForCode(600)).isEqualTo(WeatherCondition.SNOW);
        assertThat(WeatherCondition.getWeatherConditionForCode(622)).isEqualTo(WeatherCondition.SNOW);
    }

    @Test
    void code_7xx_returnsAtmosphere() {
        assertThat(WeatherCondition.getWeatherConditionForCode(700)).isEqualTo(WeatherCondition.ATMOSPHERE);
        assertThat(WeatherCondition.getWeatherConditionForCode(781)).isEqualTo(WeatherCondition.ATMOSPHERE);
    }

    @Test
    void code_800_returnsClear() {
        assertThat(WeatherCondition.getWeatherConditionForCode(800)).isEqualTo(WeatherCondition.CLEAR);
    }

    @Test
    void code_8xx_exceptClear_returnsClouds() {
        assertThat(WeatherCondition.getWeatherConditionForCode(801)).isEqualTo(WeatherCondition.CLOUDS);
        assertThat(WeatherCondition.getWeatherConditionForCode(804)).isEqualTo(WeatherCondition.CLOUDS);
    }

    @Test
    void code_unknown_returnsUndefined() {
        assertThat(WeatherCondition.getWeatherConditionForCode(999)).isEqualTo(WeatherCondition.UNDEFINED);
        assertThat(WeatherCondition.getWeatherConditionForCode(100)).isEqualTo(WeatherCondition.UNDEFINED);
    }

    @Test
    void code_null_returnsUndefined() {
        assertThat(WeatherCondition.getWeatherConditionForCode(null)).isEqualTo(WeatherCondition.UNDEFINED);
    }

}
