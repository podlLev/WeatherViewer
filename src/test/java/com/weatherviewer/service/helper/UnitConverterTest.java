package com.weatherviewer.service.helper;

import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import com.weatherviewer.model.enums.UnitSystem;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnitConverterTest {

    private final UnitConverter converter = new UnitConverter();

    private static final Date NOW = new Date();

    private WeatherDto metricWeather(double temperature, double feelsLike, double min, double max,
                                     double windSpeed, double windGust) {
        return new WeatherDto()
                .setWeatherCondition(WeatherCondition.CLEAR)
                .setTimeOfDay(TimeOfDay.DAY)
                .setDescription("clear sky")
                .setTemperature(temperature)
                .setTemperatureFeelsLike(feelsLike)
                .setTemperatureMinimum(min)
                .setTemperatureMaximum(max)
                .setHumidity(50)
                .setPressure(1013)
                .setWindSpeed(windSpeed)
                .setWindDirection(180)
                .setWindGust(windGust)
                .setCloudiness(20)
                .setDate(NOW);
    }

    @Test
    void toDisplayUnits_metric_returnsSameInstanceUnchanged() {
        WeatherDto weather = metricWeather(20.0, 19.0, 15.0, 25.0, 5.0, 8.0);

        WeatherDto result = converter.toDisplayUnits(weather, UnitSystem.METRIC);

        assertThat(result).isSameAs(weather);
    }

    @Test
    void toDisplayUnits_nullWeather_returnsNull() {
        assertThat(converter.toDisplayUnits((WeatherDto) null, UnitSystem.IMPERIAL)).isNull();
    }

    @Test
    void toDisplayUnits_imperial_convertsTemperatureToFahrenheit() {
        WeatherDto weather = metricWeather(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        WeatherDto result = converter.toDisplayUnits(weather, UnitSystem.IMPERIAL);

        assertThat(result.getTemperature()).isEqualTo(32.0);
    }

    @Test
    void toDisplayUnits_imperial_convertsHundredCelsiusCorrectly() {
        WeatherDto weather = metricWeather(100.0, 100.0, 100.0, 100.0, 0.0, 0.0);

        WeatherDto result = converter.toDisplayUnits(weather, UnitSystem.IMPERIAL);

        assertThat(result.getTemperature()).isEqualTo(212.0);
    }

    @Test
    void toDisplayUnits_imperial_convertsFeelsLikeMinAndMaxTemperatures() {
        WeatherDto weather = metricWeather(20.0, 18.5, 15.0, 25.0, 0.0, 0.0);

        WeatherDto result = converter.toDisplayUnits(weather, UnitSystem.IMPERIAL);

        assertThat(result.getTemperatureFeelsLike()).isEqualTo(65.3);
        assertThat(result.getTemperatureMinimum()).isEqualTo(59.0);
        assertThat(result.getTemperatureMaximum()).isEqualTo(77.0);
    }

    @Test
    void toDisplayUnits_imperial_convertsWindSpeedAndGustToMph() {
        WeatherDto weather = metricWeather(0.0, 0.0, 0.0, 0.0, 5.0, 10.0);

        WeatherDto result = converter.toDisplayUnits(weather, UnitSystem.IMPERIAL);

        assertThat(result.getWindSpeed()).isEqualTo(11.2);
        assertThat(result.getWindGust()).isEqualTo(22.4);
    }

    @Test
    void toDisplayUnits_imperial_roundsTemperatureToOneDecimalPlace() {
        WeatherDto weather = metricWeather(18.5, 0.0, 0.0, 0.0, 0.0, 0.0);

        WeatherDto result = converter.toDisplayUnits(weather, UnitSystem.IMPERIAL);

        assertThat(result.getTemperature()).isEqualTo(65.3);
    }

    @Test
    void toDisplayUnits_imperial_roundsHalfUp() {
        WeatherDto weather = metricWeather(0.05, 0.0, 0.0, 0.0, 0.0, 0.0);

        WeatherDto result = converter.toDisplayUnits(weather, UnitSystem.IMPERIAL);

        assertThat(result.getTemperature()).isEqualTo(32.1);
    }

    @Test
    void toDisplayUnits_imperial_windSpeedRoundedToOneDecimalPlace() {
        WeatherDto weather = metricWeather(0.0, 0.0, 0.0, 0.0, 3.6, 0.0);

        WeatherDto result = converter.toDisplayUnits(weather, UnitSystem.IMPERIAL);

        assertThat(result.getWindSpeed()).isEqualTo(8.1);
    }

    @Test
    void toDisplayUnits_imperial_nullMinMaxTemperature_staysNull() {
        WeatherDto weather = metricWeather(20.0, 19.0, 15.0, 25.0, 5.0, 8.0)
                .setTemperatureMinimum(null)
                .setTemperatureMaximum(null);

        WeatherDto result = converter.toDisplayUnits(weather, UnitSystem.IMPERIAL);

        assertThat(result.getTemperatureMinimum()).isNull();
        assertThat(result.getTemperatureMaximum()).isNull();
    }

    @Test
    void toDisplayUnits_imperial_nullWindGust_staysNull() {
        WeatherDto weather = metricWeather(20.0, 19.0, 15.0, 25.0, 5.0, 8.0)
                .setWindGust(null);

        WeatherDto result = converter.toDisplayUnits(weather, UnitSystem.IMPERIAL);

        assertThat(result.getWindGust()).isNull();
    }

    @Test
    void toDisplayUnits_imperial_doesNotMutateOriginalInstance() {
        WeatherDto weather = metricWeather(20.0, 19.0, 15.0, 25.0, 5.0, 8.0);

        converter.toDisplayUnits(weather, UnitSystem.IMPERIAL);

        assertThat(weather.getTemperature()).isEqualTo(20.0);
        assertThat(weather.getWindSpeed()).isEqualTo(5.0);
    }

    @Test
    void toDisplayUnits_imperial_returnsDifferentInstance() {
        WeatherDto weather = metricWeather(20.0, 19.0, 15.0, 25.0, 5.0, 8.0);

        WeatherDto result = converter.toDisplayUnits(weather, UnitSystem.IMPERIAL);

        assertThat(result).isNotSameAs(weather);
    }

    @Test
    void toDisplayUnits_imperial_preservesUnitIndependentFields() {
        WeatherDto weather = metricWeather(20.0, 19.0, 15.0, 25.0, 5.0, 8.0);

        WeatherDto result = converter.toDisplayUnits(weather, UnitSystem.IMPERIAL);

        assertThat(result.getWeatherCondition()).isEqualTo(WeatherCondition.CLEAR);
        assertThat(result.getTimeOfDay()).isEqualTo(TimeOfDay.DAY);
        assertThat(result.getDescription()).isEqualTo("clear sky");
        assertThat(result.getHumidity()).isEqualTo(50);
        assertThat(result.getPressure()).isEqualTo(1013);
        assertThat(result.getWindDirection()).isEqualTo(180);
        assertThat(result.getCloudiness()).isEqualTo(20);
        assertThat(result.getDate()).isEqualTo(NOW);
    }

    @Test
    void toDisplayUnits_listMetric_returnsSameListUnchanged() {
        List<WeatherDto> list = List.of(metricWeather(20.0, 19.0, 15.0, 25.0, 5.0, 8.0));

        List<WeatherDto> result = converter.toDisplayUnits(list, UnitSystem.METRIC);

        assertThat(result).isSameAs(list);
    }

    @Test
    void toDisplayUnits_nullList_returnsNull() {
        assertThat(converter.toDisplayUnits((List<WeatherDto>) null, UnitSystem.IMPERIAL)).isNull();
    }

    @Test
    void toDisplayUnits_listImperial_convertsEveryEntry() {
        List<WeatherDto> list = List.of(
                metricWeather(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                metricWeather(100.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        );

        List<WeatherDto> result = converter.toDisplayUnits(list, UnitSystem.IMPERIAL);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTemperature()).isEqualTo(32.0);
        assertThat(result.get(1).getTemperature()).isEqualTo(212.0);
    }

    @Test
    void temperatureSymbol_metric_returnsCelsiusSymbol() {
        assertThat(converter.temperatureSymbol(UnitSystem.METRIC)).isEqualTo("°C");
    }

    @Test
    void temperatureSymbol_imperial_returnsFahrenheitSymbol() {
        assertThat(converter.temperatureSymbol(UnitSystem.IMPERIAL)).isEqualTo("°F");
    }

    @Test
    void windSpeedUnit_metric_returnsMetersPerSecond() {
        assertThat(converter.windSpeedUnit(UnitSystem.METRIC)).isEqualTo("m/s");
    }

    @Test
    void windSpeedUnit_imperial_returnsMph() {
        assertThat(converter.windSpeedUnit(UnitSystem.IMPERIAL)).isEqualTo("mph");
    }

}
