package com.weatherviewer.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.weatherviewer.dto.helper.ValidatorTestFactory.assertFieldHasViolation;
import static com.weatherviewer.dto.helper.ValidatorTestFactory.assertNoViolations;
import static org.assertj.core.api.Assertions.assertThat;

class ResetPasswordDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private ResetPasswordDto validDto() {
        return new ResetPasswordDto()
                .setToken("raw-token")
                .setPassword("Secure1@")
                .setRepeatPassword("Secure1@");
    }

    @Test
    void valid_dto_hasNoViolations() {
        assertNoViolations(validator, validDto());
    }

    @Test
    void token_null_failsValidation() {
        ResetPasswordDto dto = validDto().setToken(null);
        assertFieldHasViolation(validator, dto, "token");
    }

    @Test
    void token_blank_failsValidation() {
        ResetPasswordDto dto = validDto().setToken("");
        assertFieldHasViolation(validator, dto, "token");
    }

    @Test
    void password_null_failsValidation() {
        ResetPasswordDto dto = validDto().setPassword(null);
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_blank_failsValidation() {
        ResetPasswordDto dto = validDto().setPassword("");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_noUpperCase_failsValidation() {
        ResetPasswordDto dto = validDto().setPassword("secure1@");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_noLowerCase_failsValidation() {
        ResetPasswordDto dto = validDto().setPassword("SECURE1@");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_noDigit_failsValidation() {
        ResetPasswordDto dto = validDto().setPassword("Secure@@");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_noSpecialChar_failsValidation() {
        ResetPasswordDto dto = validDto().setPassword("Secure11");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_tooShort_failsValidation() {
        ResetPasswordDto dto = validDto().setPassword("Se1@");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_exceedsMaxLength_failsValidation() {
        // 71 letters (upper/lower alternating) + digit + symbol = 73 chars, over the 72 max
        String tooLong = "Aa".repeat(35) + "A1@";
        ResetPasswordDto dto = validDto().setPassword(tooLong);
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void repeatPassword_null_failsValidation() {
        ResetPasswordDto dto = validDto().setRepeatPassword(null);
        assertFieldHasViolation(validator, dto, "repeatPassword");
    }

    @Test
    void repeatPassword_blank_failsValidation() {
        ResetPasswordDto dto = validDto().setRepeatPassword("");
        assertFieldHasViolation(validator, dto, "repeatPassword");
    }

    @Test
    void repeatPassword_exceedsMaxLength_failsValidation() {
        ResetPasswordDto dto = validDto().setRepeatPassword("A".repeat(73));
        assertFieldHasViolation(validator, dto, "repeatPassword");
    }

    @Test
    void mismatchedPasswords_notEnforcedByBeanValidation_onlyByControllerLogic() {
        ResetPasswordDto dto = validDto().setRepeatPassword("Different1@");
        assertNoViolations(validator, dto);
    }

    @Test
    void toString_excludesTokenAndPasswordFields() {
        String toString = validDto().toString();

        assertThat(toString)
                .doesNotContain("raw-token")
                .doesNotContain("Secure1@");
    }

}
