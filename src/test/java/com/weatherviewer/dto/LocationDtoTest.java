package com.weatherviewer.dto;

import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.weatherviewer.dto.helper.ValidatorTestFactory.*;

class LocationDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = skipValidator();
    }

    private LocationDto validDto() {
        return new LocationDto()
                .setId(UUID.randomUUID())
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setUserId(UUID.randomUUID())
                .setFavorite(false)
                .setCreatedAt(LocalDateTime.now());
    }

    @Test
    void valid_dto_hasNoViolations() {
        assertNoViolations(validator, validDto());
    }

    @Test
    void id_null_failsValidation() {
        LocationDto dto = validDto().setId(null);
        assertFieldHasViolation(validator, dto, "id");
    }

    @Test
    void name_null_failsValidation() {
        LocationDto dto = validDto().setName(null);
        assertFieldHasViolation(validator, dto, "name");
    }

    @Test
    void name_blank_failsValidation() {
        LocationDto dto = validDto().setName("");
        assertFieldHasViolation(validator, dto, "name");
    }

    @Test
    void name_whitespaceOnly_failsValidation() {
        LocationDto dto = validDto().setName("   ");
        assertFieldHasViolation(validator, dto, "name");
    }

    @Test
    void name_exactlyMaxLength_passesValidation() {
        LocationDto dto = validDto().setName("A".repeat(100));
        assertNoViolations(validator, dto);
    }

    @Test
    void name_exceedsMaxLength_failsValidation() {
        LocationDto dto = validDto().setName("A".repeat(101));
        assertFieldHasViolation(validator, dto, "name");
    }

    @Test
    void latitude_null_failsValidation() {
        LocationDto dto = validDto().setLatitude(null);
        assertFieldHasViolation(validator, dto, "latitude");
    }

    @Test
    void latitude_tooLow_failsValidation() {
        LocationDto dto = validDto().setLatitude(-91.0);
        assertFieldHasViolation(validator, dto, "latitude");
    }

    @Test
    void latitude_tooHigh_failsValidation() {
        LocationDto dto = validDto().setLatitude(91.0);
        assertFieldHasViolation(validator, dto, "latitude");
    }

    @Test
    void latitude_atLowerBoundary_passesValidation() {
        LocationDto dto = validDto().setLatitude(-90.0);
        assertNoViolations(validator, dto);
    }

    @Test
    void latitude_atUpperBoundary_passesValidation() {
        LocationDto dto = validDto().setLatitude(90.0);
        assertNoViolations(validator, dto);
    }

    @Test
    void longitude_null_failsValidation() {
        LocationDto dto = validDto().setLongitude(null);
        assertFieldHasViolation(validator, dto, "longitude");
    }

    @Test
    void longitude_tooLow_failsValidation() {
        LocationDto dto = validDto().setLongitude(-181.0);
        assertFieldHasViolation(validator, dto, "longitude");
    }

    @Test
    void longitude_tooHigh_failsValidation() {
        LocationDto dto = validDto().setLongitude(181.0);
        assertFieldHasViolation(validator, dto, "longitude");
    }

    @Test
    void longitude_atLowerBoundary_passesValidation() {
        LocationDto dto = validDto().setLongitude(-180.0);
        assertNoViolations(validator, dto);
    }

    @Test
    void longitude_atUpperBoundary_passesValidation() {
        LocationDto dto = validDto().setLongitude(180.0);
        assertNoViolations(validator, dto);
    }

    @Test
    void userId_null_failsValidation() {
        LocationDto dto = validDto().setUserId(null);
        assertFieldHasViolation(validator, dto, "userId");
    }

    @Test
    void favorite_null_failsValidation() {
        LocationDto dto = validDto().setFavorite(null);
        assertFieldHasViolation(validator, dto, "favorite");
    }

    @Test
    void createdAt_null_failsValidation() {
        LocationDto dto = validDto().setCreatedAt(null);
        assertFieldHasViolation(validator, dto, "createdAt");
    }

}
