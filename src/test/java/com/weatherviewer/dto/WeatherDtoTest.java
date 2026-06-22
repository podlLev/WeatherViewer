package com.weatherviewer.dto;

import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static com.weatherviewer.dto.helper.ValidatorTestFactory.*;

class WeatherDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = skipValidator();
    }

    private WeatherDto validDto() {
        return new WeatherDto()
                .setWeatherCondition(WeatherCondition.CLEAR)
                .setTimeOfDay(TimeOfDay.DAY)
                .setDescription("Sunny day")
                .setTemperature(25.0)
                .setTemperatureFeelsLike(24.0)
                .setDate(new Date());
    }

    @Test
    void valid_dto_hasNoViolations() {
        assertNoViolations(validator, validDto());
    }

    @Test
    void weatherCondition_null_failsValidation() {
        WeatherDto dto = validDto().setWeatherCondition(null);
        assertFieldHasViolation(validator, dto, "weatherCondition");
    }

    @Test
    void timeOfDay_null_failsValidation() {
        WeatherDto dto = validDto().setTimeOfDay(null);
        assertFieldHasViolation(validator, dto, "timeOfDay");
    }

    @Test
    void description_null_failsValidation() {
        WeatherDto dto = validDto().setDescription(null);
        assertFieldHasViolation(validator, dto, "description");
    }

    @Test
    void description_blank_failsValidation() {
        WeatherDto dto = validDto().setDescription("");
        assertFieldHasViolation(validator, dto, "description");
    }

    @Test
    void description_exceedsMaxLength_failsValidation() {
        WeatherDto dto = validDto().setDescription("a".repeat(256));
        assertFieldHasViolation(validator, dto, "description");
    }

    @Test
    void description_exactlyMaxLength_passesValidation() {
        WeatherDto dto = validDto().setDescription("a".repeat(255));
        assertNoViolations(validator, dto);
    }

    @Test
    void temperature_null_failsValidation() {
        WeatherDto dto = validDto().setTemperature(null);
        assertFieldHasViolation(validator, dto, "temperature");
    }

    @Test
    void temperature_tooLow_failsValidation() {
        WeatherDto dto = validDto().setTemperature(-101.0);
        assertFieldHasViolation(validator, dto, "temperature");
    }

    @Test
    void temperature_tooHigh_failsValidation() {
        WeatherDto dto = validDto().setTemperature(61.0);
        assertFieldHasViolation(validator, dto, "temperature");
    }

    @Test
    void temperature_atLowerBoundary_passesValidation() {
        WeatherDto dto = validDto().setTemperature(-100.0);
        assertNoViolations(validator, dto);
    }

    @Test
    void temperature_atUpperBoundary_passesValidation() {
        WeatherDto dto = validDto().setTemperature(60.0);
        assertNoViolations(validator, dto);
    }

    @Test
    void temperatureFeelsLike_null_failsValidation() {
        WeatherDto dto = validDto().setTemperatureFeelsLike(null);
        assertFieldHasViolation(validator, dto, "temperatureFeelsLike");
    }

    @Test
    void temperatureFeelsLike_tooLow_failsValidation() {
        WeatherDto dto = validDto().setTemperatureFeelsLike(-101.0);
        assertFieldHasViolation(validator, dto, "temperatureFeelsLike");
    }

    @Test
    void temperatureFeelsLike_tooHigh_failsValidation() {
        WeatherDto dto = validDto().setTemperatureFeelsLike(61.0);
        assertFieldHasViolation(validator, dto, "temperatureFeelsLike");
    }

    @Test
    void temperatureMinimum_null_passesValidation() {
        WeatherDto dto = validDto().setTemperatureMinimum(null);
        assertNoViolations(validator, dto);
    }

    @Test
    void temperatureMinimum_tooLow_failsValidation() {
        WeatherDto dto = validDto().setTemperatureMinimum(-101.0);
        assertFieldHasViolation(validator, dto, "temperatureMinimum");
    }

    @Test
    void temperatureMinimum_tooHigh_failsValidation() {
        WeatherDto dto = validDto().setTemperatureMinimum(61.0);
        assertFieldHasViolation(validator, dto, "temperatureMinimum");
    }

    @Test
    void temperatureMaximum_null_passesValidation() {
        WeatherDto dto = validDto().setTemperatureMaximum(null);
        assertNoViolations(validator, dto);
    }

    @Test
    void temperatureMaximum_tooLow_failsValidation() {
        WeatherDto dto = validDto().setTemperatureMaximum(-101.0);
        assertFieldHasViolation(validator, dto, "temperatureMaximum");
    }

    @Test
    void temperatureMaximum_tooHigh_failsValidation() {
        WeatherDto dto = validDto().setTemperatureMaximum(61.0);
        assertFieldHasViolation(validator, dto, "temperatureMaximum");
    }

    @Test
    void humidity_negative_failsValidation() {
        WeatherDto dto = validDto().setHumidity(-1);
        assertFieldHasViolation(validator, dto, "humidity");
    }

    @Test
    void humidity_tooHigh_failsValidation() {
        WeatherDto dto = validDto().setHumidity(101);
        assertFieldHasViolation(validator, dto, "humidity");
    }

    @Test
    void humidity_atLowerBoundary_passesValidation() {
        WeatherDto dto = validDto().setHumidity(0);
        assertNoViolations(validator, dto);
    }

    @Test
    void humidity_atUpperBoundary_passesValidation() {
        WeatherDto dto = validDto().setHumidity(100);
        assertNoViolations(validator, dto);
    }

    @Test
    void pressure_negative_failsValidation() {
        WeatherDto dto = validDto().setPressure(-1);
        assertFieldHasViolation(validator, dto, "pressure");
    }

    @Test
    void pressure_zero_passesValidation() {
        WeatherDto dto = validDto().setPressure(0);
        assertNoViolations(validator, dto);
    }

    @Test
    void windSpeed_negative_failsValidation() {
        WeatherDto dto = validDto().setWindSpeed(-1.0);
        assertFieldHasViolation(validator, dto, "windSpeed");
    }

    @Test
    void windSpeed_zero_passesValidation() {
        WeatherDto dto = validDto().setWindSpeed(0.0);
        assertNoViolations(validator, dto);
    }

    @Test
    void windDirection_negative_failsValidation() {
        WeatherDto dto = validDto().setWindDirection(-1);
        assertFieldHasViolation(validator, dto, "windDirection");
    }

    @Test
    void windDirection_tooHigh_failsValidation() {
        WeatherDto dto = validDto().setWindDirection(361);
        assertFieldHasViolation(validator, dto, "windDirection");
    }

    @Test
    void windDirection_atLowerBoundary_passesValidation() {
        WeatherDto dto = validDto().setWindDirection(0);
        assertNoViolations(validator, dto);
    }

    @Test
    void windDirection_atUpperBoundary_passesValidation() {
        WeatherDto dto = validDto().setWindDirection(360);
        assertNoViolations(validator, dto);
    }

    @Test
    void windGust_negative_failsValidation() {
        WeatherDto dto = validDto().setWindGust(-1.0);
        assertFieldHasViolation(validator, dto, "windGust");
    }

    @Test
    void cloudiness_negative_failsValidation() {
        WeatherDto dto = validDto().setCloudiness(-1);
        assertFieldHasViolation(validator, dto, "cloudiness");
    }

    @Test
    void cloudiness_tooHigh_failsValidation() {
        WeatherDto dto = validDto().setCloudiness(101);
        assertFieldHasViolation(validator, dto, "cloudiness");
    }

    @Test
    void date_null_failsValidation() {
        WeatherDto dto = validDto().setDate(null);
        assertFieldHasViolation(validator, dto, "date");
    }

    @Test
    void sunrise_null_passesValidation() {
        WeatherDto dto = validDto().setSunrise(null);
        assertNoViolations(validator, dto);
    }

    @Test
    void sunset_null_passesValidation() {
        WeatherDto dto = validDto().setSunset(null);
        assertNoViolations(validator, dto);
    }

}
