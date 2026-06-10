package com.weatherviewer.dto;

import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.weatherviewer.dto.helper.ValidatorTestFactory.*;

class GeoLocationDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = skipValidator();
    }

    private GeoLocationDto validDto() {
        return new GeoLocationDto()
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52);
    }

    @Test
    void valid_dto_hasNoViolations() {
        assertNoViolations(validator, validDto());
    }

    @Test
    void name_null_failsValidation() {
        GeoLocationDto dto = validDto().setName(null);
        assertFieldHasViolation(validator, dto, "name");
    }

    @Test
    void name_blank_failsValidation() {
        GeoLocationDto dto = validDto().setName("");
        assertFieldHasViolation(validator, dto, "name");
    }

    @Test
    void name_whitespaceOnly_failsValidation() {
        GeoLocationDto dto = validDto().setName("   ");
        assertFieldHasViolation(validator, dto, "name");
    }

    @Test
    void name_exactlyMaxLength_passesValidation() {
        GeoLocationDto dto = validDto().setName("A".repeat(100));
        assertNoViolations(validator, dto);
    }

    @Test
    void name_exceedsMaxLength_failsValidation() {
        GeoLocationDto dto = validDto().setName("A".repeat(101));
        assertFieldHasViolation(validator, dto, "name");
    }

    @Test
    void latitude_null_failsValidation() {
        GeoLocationDto dto = validDto().setLatitude(null);
        assertFieldHasViolation(validator, dto, "latitude");
    }

    @Test
    void latitude_tooLow_failsValidation() {
        GeoLocationDto dto = validDto().setLatitude(-91.0);
        assertFieldHasViolation(validator, dto, "latitude");
    }

    @Test
    void latitude_tooHigh_failsValidation() {
        GeoLocationDto dto = validDto().setLatitude(91.0);
        assertFieldHasViolation(validator, dto, "latitude");
    }

    @Test
    void latitude_atLowerBoundary_passesValidation() {
        GeoLocationDto dto = validDto().setLatitude(-90.0);
        assertNoViolations(validator, dto);
    }

    @Test
    void latitude_atUpperBoundary_passesValidation() {
        GeoLocationDto dto = validDto().setLatitude(90.0);
        assertNoViolations(validator, dto);
    }

    @Test
    void longitude_null_failsValidation() {
        GeoLocationDto dto = validDto().setLongitude(null);
        assertFieldHasViolation(validator, dto, "longitude");
    }

    @Test
    void longitude_tooLow_failsValidation() {
        GeoLocationDto dto = validDto().setLongitude(-181.0);
        assertFieldHasViolation(validator, dto, "longitude");
    }

    @Test
    void longitude_tooHigh_failsValidation() {
        GeoLocationDto dto = validDto().setLongitude(181.0);
        assertFieldHasViolation(validator, dto, "longitude");
    }

    @Test
    void longitude_atLowerBoundary_passesValidation() {
        GeoLocationDto dto = validDto().setLongitude(-180.0);
        assertNoViolations(validator, dto);
    }

    @Test
    void longitude_atUpperBoundary_passesValidation() {
        GeoLocationDto dto = validDto().setLongitude(180.0);
        assertNoViolations(validator, dto);
    }

    @Test
    void country_whitespaceOnly_failsValidation() {
        GeoLocationDto dto = validDto().setCountry("   ");
        assertFieldHasViolation(validator, dto, "country");
    }

    @Test
    void country_null_passesValidation() {
        GeoLocationDto dto = validDto().setCountry(null);
        assertNoViolations(validator, dto);
    }

    @Test
    void country_valid_passesValidation() {
        GeoLocationDto dto = validDto().setCountry("Ukraine");
        assertNoViolations(validator, dto);
    }

    @Test
    void state_null_passesValidation() {
        GeoLocationDto dto = validDto().setState(null);
        assertNoViolations(validator, dto);
    }

    @Test
    void state_any_passesValidation() {
        GeoLocationDto dto = validDto().setState("Kyiv Oblast");
        assertNoViolations(validator, dto);
    }

}
