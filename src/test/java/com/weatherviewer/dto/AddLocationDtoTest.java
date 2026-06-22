package com.weatherviewer.dto;

import com.weatherviewer.validation.validator.UniqueLocationValidator;
import jakarta.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.weatherviewer.dto.helper.ValidatorTestFactory.*;

class AddLocationDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = skipValidator(UniqueLocationValidator.class);
    }

    private AddLocationDto validDto() {
        return new AddLocationDto()
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setUserId(UUID.randomUUID());
    }

    @Test
    void valid_dto_hasNoViolations() {
        assertNoViolations(validator, validDto());
    }

    @Test
    void name_null_failsValidation() {
        AddLocationDto dto = validDto().setName(null);
        assertFieldHasViolation(validator, dto, "name");
    }

    @Test
    void name_whitespaceOnly_failsValidation() {
        AddLocationDto dto = validDto().setName("   ");
        assertFieldHasViolation(validator, dto, "name");
    }

    @Test
    void name_exceedsMaxLength_failsValidation() {
        AddLocationDto dto = validDto().setName("A".repeat(101));
        assertFieldHasViolation(validator, dto, "name");
    }

    @Test
    void name_exactlyMaxLength_passesValidation() {
        AddLocationDto dto = validDto().setName("A".repeat(100));
        assertNoViolations(validator, dto);
    }

    @Test
    void name_oneCharacter_passesValidation() {
        AddLocationDto dto = validDto().setName("A");
        assertNoViolations(validator, dto);
    }

    @Test
    void latitude_null_failsValidation() {
        AddLocationDto dto = validDto().setLatitude(null);
        assertFieldHasViolation(validator, dto, "latitude");
    }

    @Test
    void latitude_tooLow_failsValidation() {
        AddLocationDto dto = validDto().setLatitude(-91.0);
        assertFieldHasViolation(validator, dto, "latitude");
    }

    @Test
    void latitude_tooHigh_failsValidation() {
        AddLocationDto dto = validDto().setLatitude(91.0);
        assertFieldHasViolation(validator, dto, "latitude");
    }

    @Test
    void latitude_atBoundary_passesValidation() {
        assertNoViolations(validator, validDto().setLatitude(-90.0));
        assertNoViolations(validator, validDto().setLatitude(90.0));
    }

    @Test
    void longitude_null_failsValidation() {
        AddLocationDto dto = validDto().setLongitude(null);
        assertFieldHasViolation(validator, dto, "longitude");
    }

    @Test
    void longitude_tooLow_failsValidation() {
        AddLocationDto dto = validDto().setLongitude(-181.0);
        assertFieldHasViolation(validator, dto, "longitude");
    }

    @Test
    void longitude_tooHigh_failsValidation() {
        AddLocationDto dto = validDto().setLongitude(181.0);
        assertFieldHasViolation(validator, dto, "longitude");
    }

    @Test
    void longitude_atBoundary_passesValidation() {
        assertNoViolations(validator, validDto().setLongitude(-180.0));
        assertNoViolations(validator, validDto().setLongitude(180.0));
    }

    @Test
    void userId_null_hasNoViolations() {
        AddLocationDto dto = validDto().setUserId(null);
        assertNoViolations(validator, dto);
    }

    @Test
    void userId_valid_passesValidation() {
        AddLocationDto dto = validDto().setUserId(UUID.randomUUID());
        assertNoViolations(validator, dto);
    }

}
