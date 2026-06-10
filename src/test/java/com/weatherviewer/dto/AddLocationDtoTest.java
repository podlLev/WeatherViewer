package com.weatherviewer.dto;

import com.weatherviewer.dto.helper.ValidatorTestFactory;
import com.weatherviewer.validation.validator.UniqueLocationValidator;
import jakarta.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AddLocationDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = ValidatorTestFactory.skipValidator(UniqueLocationValidator.class);
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
        assertThat(validator.validate(validDto())).isEmpty();
    }

    @Test
    void name_null_failsValidation() {
        AddLocationDto dto = validDto().setName(null);
        assertThat(validator.validate(dto))
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void name_whitespaceOnly_failsValidation() {
        AddLocationDto dto = validDto().setName("   ");
        assertThat(validator.validate(dto))
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void name_exactlyMaxLength_passesValidation() {
        AddLocationDto dto = validDto().setName("A".repeat(100));
        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void name_oneCharacter_passesValidation() {
        AddLocationDto dto = validDto().setName("A");
        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void latitude_tooLow_failsValidation() {
        AddLocationDto dto = validDto().setLatitude(-91.0);
        assertThat(validator.validate(dto))
                .anyMatch(v -> v.getPropertyPath().toString().equals("latitude"));
    }

    @Test
    void latitude_tooHigh_failsValidation() {
        AddLocationDto dto = validDto().setLatitude(91.0);
        assertThat(validator.validate(dto))
                .anyMatch(v -> v.getPropertyPath().toString().equals("latitude"));
    }

    @Test
    void latitude_atBoundary_passesValidation() {
        assertThat(validator.validate(validDto().setLatitude(-90.0))).isEmpty();
        assertThat(validator.validate(validDto().setLatitude(90.0))).isEmpty();
    }

    @Test
    void longitude_tooLow_failsValidation() {
        AddLocationDto dto = validDto().setLongitude(-181.0);
        assertThat(validator.validate(dto))
                .anyMatch(v -> v.getPropertyPath().toString().equals("longitude"));
    }

    @Test
    void longitude_tooHigh_failsValidation() {
        AddLocationDto dto = validDto().setLongitude(181.0);
        assertThat(validator.validate(dto))
                .anyMatch(v -> v.getPropertyPath().toString().equals("longitude"));
    }

    @Test
    void longitude_atBoundary_passesValidation() {
        assertThat(validator.validate(validDto().setLongitude(-180.0))).isEmpty();
        assertThat(validator.validate(validDto().setLongitude(180.0))).isEmpty();
    }

    @Test
    void userId_null_failsValidation() {
        AddLocationDto dto = validDto().setUserId(null);
        assertThat(validator.validate(dto))
                .anyMatch(v -> v.getPropertyPath().toString().equals("userId"));
    }

    @Test
    void userId_valid_passesValidation() {
        AddLocationDto dto = validDto().setUserId(UUID.randomUUID());
        assertThat(validator.validate(dto)).isEmpty();
    }

}
