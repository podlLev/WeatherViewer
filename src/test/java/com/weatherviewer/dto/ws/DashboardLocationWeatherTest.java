package com.weatherviewer.dto.ws;

import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardLocationWeatherTest {

    private WeatherDto weatherDto() {
        return new WeatherDto()
                .setWeatherCondition(WeatherCondition.CLEAR)
                .setTimeOfDay(TimeOfDay.DAY)
                .setDescription("Clear sky")
                .setTemperature(25.0)
                .setTemperatureFeelsLike(24.0)
                .setDate(new Date());
    }

    @Test
    void chainedSetters_returnSameInstanceAndPersistValues() {
        UUID locationId = UUID.randomUUID();
        WeatherDto weather = weatherDto();

        DashboardLocationWeather result = new DashboardLocationWeather()
                .setLocationId(locationId)
                .setLocationName("Kyiv")
                .setWeather(weather);

        assertThat(result.getLocationId()).isEqualTo(locationId);
        assertThat(result.getLocationName()).isEqualTo("Kyiv");
        assertThat(result.getWeather()).isEqualTo(weather);
    }

    @Test
    void newInstance_hasNullFields() {
        DashboardLocationWeather result = new DashboardLocationWeather();

        assertThat(result.getLocationId()).isNull();
        assertThat(result.getLocationName()).isNull();
        assertThat(result.getWeather()).isNull();
    }

}
