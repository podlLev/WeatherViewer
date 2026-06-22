package com.weatherviewer.dto;

import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.weatherviewer.dto.helper.ValidatorTestFactory.*;

class UpdateUserDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = skipValidator();
    }

    private UpdateUserDto validDto() {
        return new UpdateUserDto()
                .setEmail("john@example.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword("Secure1@");
    }

    @Test
    void valid_dto_hasNoViolations() {
        assertNoViolations(validator, validDto());
    }

    @Test
    void email_null_failsValidation() {
        UpdateUserDto dto = validDto().setEmail(null);
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_blank_failsValidation() {
        UpdateUserDto dto = validDto().setEmail("");
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_whitespaceOnly_failsValidation() {
        UpdateUserDto dto = validDto().setEmail("   ");
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_invalidFormat_failsValidation() {
        UpdateUserDto dto = validDto().setEmail("not-an-email");
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_exceedsMaxLength_failsValidation() {
        UpdateUserDto dto = validDto().setEmail("a".repeat(145) + "@x.com");
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_validFormat_passesValidation() {
        UpdateUserDto dto = validDto().setEmail("valid@example.com");
        assertNoViolations(validator, dto);
    }

    @Test
    void firstName_null_failsValidation() {
        UpdateUserDto dto = validDto().setFirstName(null);
        assertFieldHasViolation(validator, dto, "firstName");
    }

    @Test
    void firstName_blank_failsValidation() {
        UpdateUserDto dto = validDto().setFirstName("");
        assertFieldHasViolation(validator, dto, "firstName");
    }

    @Test
    void firstName_exceedsMaxLength_failsValidation() {
        UpdateUserDto dto = validDto().setFirstName("A".repeat(51));
        assertFieldHasViolation(validator, dto, "firstName");
    }

    @Test
    void firstName_exactlyMaxLength_passesValidation() {
        UpdateUserDto dto = validDto().setFirstName("A".repeat(50));
        assertNoViolations(validator, dto);
    }

    @Test
    void lastName_null_failsValidation() {
        UpdateUserDto dto = validDto().setLastName(null);
        assertFieldHasViolation(validator, dto, "lastName");
    }

    @Test
    void lastName_blank_failsValidation() {
        UpdateUserDto dto = validDto().setLastName("");
        assertFieldHasViolation(validator, dto, "lastName");
    }

    @Test
    void lastName_exceedsMaxLength_failsValidation() {
        UpdateUserDto dto = validDto().setLastName("A".repeat(51));
        assertFieldHasViolation(validator, dto, "lastName");
    }

    @Test
    void lastName_exactlyMaxLength_passesValidation() {
        UpdateUserDto dto = validDto().setLastName("A".repeat(50));
        assertNoViolations(validator, dto);
    }

    @Test
    void password_null_passesValidation() {
        UpdateUserDto dto = validDto().setPassword(null);
        assertNoViolations(validator, dto);
    }

    @Test
    void password_noUpperCase_failsValidation() {
        UpdateUserDto dto = validDto().setPassword("secure1@");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_noDigit_failsValidation() {
        UpdateUserDto dto = validDto().setPassword("Secure@@");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_noSpecialChar_failsValidation() {
        UpdateUserDto dto = validDto().setPassword("Secure11");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_tooShort_failsValidation() {
        UpdateUserDto dto = validDto().setPassword("Se1@");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_exceedsMaxLength_failsValidation() {
        UpdateUserDto dto = validDto().setPassword("Secure1@" + "a".repeat(65));
        assertFieldHasViolation(validator, dto, "password");
    }

}
