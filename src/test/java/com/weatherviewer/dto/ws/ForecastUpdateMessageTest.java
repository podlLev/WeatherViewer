package com.weatherviewer.dto.ws;

import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForecastUpdateMessageTest {

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
        WeatherDto hourly = weatherDto();
        WeatherDto daily = weatherDto();

        ForecastUpdateMessage message = new ForecastUpdateMessage()
                .setHourlyForecast(List.of(hourly))
                .setDailyForecast(List.of(daily));

        assertThat(message.getHourlyForecast()).containsExactly(hourly);
        assertThat(message.getDailyForecast()).containsExactly(daily);
    }

    @Test
    void newInstance_hasNullLists() {
        ForecastUpdateMessage message = new ForecastUpdateMessage();

        assertThat(message.getHourlyForecast()).isNull();
        assertThat(message.getDailyForecast()).isNull();
    }

}
