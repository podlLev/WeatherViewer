package com.weatherviewer.service.helper;

import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherAggregatorHelperTest {

    private final WeatherAggregatorHelper helper = new WeatherAggregatorHelper();

    private static final Date TODAY = new Date();
    private static final Date TOMORROW = new Date(System.currentTimeMillis() + 86400000L);

    private WeatherDto forecast(Date date, double temp, Double tempMin, Double tempMax, WeatherCondition condition) {
        return new WeatherDto()
                .setDate(date)
                .setTemperature(temp)
                .setTemperatureMinimum(tempMin)
                .setTemperatureMaximum(tempMax)
                .setWeatherCondition(condition)
                .setTimeOfDay(TimeOfDay.DAY);
    }

    private Date today() { return TODAY; }

    private Date tomorrow() { return TOMORROW; }

    @Test
    void aggregateDailyForecast_groupsByDay() {
        List<WeatherDto> hourly = List.of(
                forecast(today(), 20.0, 18.0, 22.0, WeatherCondition.CLEAR),
                forecast(today(), 22.0, 19.0, 24.0, WeatherCondition.CLEAR),
                forecast(tomorrow(), 15.0, 13.0, 17.0, WeatherCondition.CLOUDS)
        );

        List<WeatherDto> result = helper.aggregateDailyForecast(hourly);

        assertThat(result).hasSize(2);
    }

    @Test
    void aggregateDailyForecast_preservesOrder() {
        List<WeatherDto> hourly = List.of(
                forecast(today(), 20.0, 18.0, 22.0, WeatherCondition.CLEAR),
                forecast(tomorrow(), 15.0, 13.0, 17.0, WeatherCondition.CLOUDS)
        );

        List<WeatherDto> result = helper.aggregateDailyForecast(hourly);

        assertThat(result.get(0).getDate()).isEqualTo(today());
        assertThat(result.get(1).getDate()).isEqualTo(tomorrow());
    }

    @Test
    void aggregateDailyForecast_setsTimeOfDayUndefined() {
        List<WeatherDto> hourly = List.of(
                forecast(today(), 20.0, 18.0, 22.0, WeatherCondition.CLEAR)
        );

        List<WeatherDto> result = helper.aggregateDailyForecast(hourly);

        assertThat(result.get(0).getTimeOfDay()).isEqualTo(TimeOfDay.UNDEFINED);
    }

    @Test
    void aggregateDailyForecast_calculatesAverageTemperature() {
        List<WeatherDto> hourly = List.of(
                forecast(today(), 20.0, 18.0, 22.0, WeatherCondition.CLEAR),
                forecast(today(), 30.0, 28.0, 32.0, WeatherCondition.CLEAR)
        );

        List<WeatherDto> result = helper.aggregateDailyForecast(hourly);

        assertThat(result.get(0).getTemperature()).isEqualTo(25.0);
    }

    @Test
    void aggregateDailyForecast_calculatesMinTemperature() {
        List<WeatherDto> hourly = List.of(
                forecast(today(), 20.0, 18.0, 22.0, WeatherCondition.CLEAR),
                forecast(today(), 30.0, 10.0, 32.0, WeatherCondition.CLEAR)
        );

        List<WeatherDto> result = helper.aggregateDailyForecast(hourly);

        assertThat(result.get(0).getTemperatureMinimum()).isEqualTo(10.0);
    }

    @Test
    void aggregateDailyForecast_calculatesMaxTemperature() {
        List<WeatherDto> hourly = List.of(
                forecast(today(), 20.0, 18.0, 22.0, WeatherCondition.CLEAR),
                forecast(today(), 30.0, 28.0, 40.0, WeatherCondition.CLEAR)
        );

        List<WeatherDto> result = helper.aggregateDailyForecast(hourly);

        assertThat(result.get(0).getTemperatureMaximum()).isEqualTo(40.0);
    }

    @Test
    void aggregateDailyForecast_calculatesMostCommonWeatherCondition() {
        List<WeatherDto> hourly = List.of(
                forecast(today(), 20.0, 18.0, 22.0, WeatherCondition.CLEAR),
                forecast(today(), 22.0, 19.0, 24.0, WeatherCondition.RAIN),
                forecast(today(), 21.0, 18.0, 23.0, WeatherCondition.RAIN)
        );

        List<WeatherDto> result = helper.aggregateDailyForecast(hourly);

        assertThat(result.get(0).getWeatherCondition()).isEqualTo(WeatherCondition.RAIN);
    }

    @Test
    void aggregateDailyForecast_nullWeatherConditions_returnsUndefined() {
        List<WeatherDto> hourly = List.of(
                forecast(today(), 20.0, 18.0, 22.0, null)
        );

        List<WeatherDto> result = helper.aggregateDailyForecast(hourly);

        assertThat(result.get(0).getWeatherCondition()).isEqualTo(WeatherCondition.UNDEFINED);
    }

    @Test
    void aggregateDailyForecast_nullMinTemperatures_returnsNaN() {
        List<WeatherDto> hourly = List.of(
                forecast(today(), 20.0, null, null, WeatherCondition.CLEAR)
        );

        List<WeatherDto> result = helper.aggregateDailyForecast(hourly);

        assertThat(result.get(0).getTemperatureMinimum()).isNaN();
        assertThat(result.get(0).getTemperatureMaximum()).isNaN();
    }

    @Test
    void aggregateDailyForecast_emptyList_returnsEmpty() {
        List<WeatherDto> result = helper.aggregateDailyForecast(List.of());
        assertThat(result).isEmpty();
    }

}
