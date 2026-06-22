package com.weatherviewer.dto;

import com.weatherviewer.validation.validator.UniqueEmailValidator;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.weatherviewer.dto.helper.ValidatorTestFactory.*;

class CreateUserDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = skipValidator(UniqueEmailValidator.class);
    }

    private CreateUserDto validDto() {
        return new CreateUserDto()
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("john@example.com")
                .setPassword("Secure1@")
                .setRepeatPassword("Secure1@");
    }

    @Test
    void valid_dto_hasNoViolations() {
        assertNoViolations(validator, validDto());
    }

    @Test
    void firstName_null_failsValidation() {
        CreateUserDto dto = validDto().setFirstName(null);
        assertFieldHasViolation(validator, dto, "firstName");
    }

    @Test
    void firstName_whitespaceOnly_failsValidation() {
        CreateUserDto dto = validDto().setFirstName("   ");
        assertFieldHasViolation(validator, dto, "firstName");
    }

    @Test
    void firstName_exceedsMaxLength_failsValidation() {
        CreateUserDto dto = validDto().setFirstName("A".repeat(51));
        assertFieldHasViolation(validator, dto, "firstName");
    }

    @Test
    void firstName_exactlyMaxLength_passesValidation() {
        CreateUserDto dto = validDto().setFirstName("A".repeat(50));
        assertNoViolations(validator, dto);
    }

    @Test
    void firstName_oneCharacter_passesValidation() {
        CreateUserDto dto = validDto().setFirstName("A");
        assertNoViolations(validator, dto);
    }

    @Test
    void lastName_null_failsValidation() {
        CreateUserDto dto = validDto().setLastName(null);
        assertFieldHasViolation(validator, dto, "lastName");
    }

    @Test
    void lastName_whitespaceOnly_failsValidation() {
        CreateUserDto dto = validDto().setLastName("   ");
        assertFieldHasViolation(validator, dto, "lastName");
    }

    @Test
    void lastName_exceedsMaxLength_failsValidation() {
        CreateUserDto dto = validDto().setLastName("A".repeat(51));
        assertFieldHasViolation(validator, dto, "lastName");
    }

    @Test
    void lastName_exactlyMaxLength_passesValidation() {
        CreateUserDto dto = validDto().setLastName("A".repeat(50));
        assertNoViolations(validator, dto);
    }

    @Test
    void lastName_oneCharacter_passesValidation() {
        CreateUserDto dto = validDto().setLastName("A");
        assertNoViolations(validator, dto);
    }

    @Test
    void email_null_failsValidation() {
        CreateUserDto dto = validDto().setEmail(null);
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_blank_failsValidation() {
        CreateUserDto dto = validDto().setEmail("");
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_invalidFormat_failsValidation() {
        CreateUserDto dto = validDto().setEmail("not-an-email");
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_exceedsMaxLength_failsValidation() {
        CreateUserDto dto = validDto().setEmail("a".repeat(145) + "@x.com");
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void password_null_failsValidation() {
        CreateUserDto dto = validDto().setPassword(null);
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_blank_failsValidation() {
        CreateUserDto dto = validDto().setPassword("");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_noUpperCase_failsValidation() {
        CreateUserDto dto = validDto().setPassword("secure1@");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_noDigit_failsValidation() {
        CreateUserDto dto = validDto().setPassword("Secure@@");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_noSpecialChar_failsValidation() {
        CreateUserDto dto = validDto().setPassword("Secure11");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_tooShort_failsValidation() {
        CreateUserDto dto = validDto().setPassword("Se1@");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void passwords_doNotMatch_failsValidation() {
        CreateUserDto dto = validDto().setRepeatPassword("Different1@");
        assertFieldHasViolation(validator, dto, "repeatPassword");
    }

}