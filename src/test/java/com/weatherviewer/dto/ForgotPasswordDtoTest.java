package com.weatherviewer.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.weatherviewer.dto.helper.ValidatorTestFactory.assertFieldHasViolation;
import static com.weatherviewer.dto.helper.ValidatorTestFactory.assertNoViolations;

class ForgotPasswordDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private ForgotPasswordDto validDto() {
        return new ForgotPasswordDto().setEmail("john@example.com");
    }

    @Test
    void valid_dto_hasNoViolations() {
        assertNoViolations(validator, validDto());
    }

    @Test
    void email_null_failsValidation() {
        ForgotPasswordDto dto = validDto().setEmail(null);
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_blank_failsValidation() {
        ForgotPasswordDto dto = validDto().setEmail("");
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_whitespaceOnly_failsValidation() {
        ForgotPasswordDto dto = validDto().setEmail("   ");
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_invalidFormat_failsValidation() {
        ForgotPasswordDto dto = validDto().setEmail("not-an-email");
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_exceedsMaxLength_failsValidation() {
        ForgotPasswordDto dto = validDto().setEmail("a".repeat(145) + "@x.com");
        assertFieldHasViolation(validator, dto, "email");
    }

}
