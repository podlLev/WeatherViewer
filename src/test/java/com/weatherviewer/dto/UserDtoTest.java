package com.weatherviewer.dto;

import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UserStatus;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.weatherviewer.dto.helper.ValidatorTestFactory.*;

class UserDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = skipValidator();
    }

    private UserDto validDto() {
        return new UserDto()
                .setId(UUID.randomUUID())
                .setUsername("john_doe")
                .setEmail("john@example.com")
                .setPassword("Secure1@")
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER)
                .setLocations(List.of())
                .setCreatedAt(LocalDateTime.now());
    }

    @Test
    void valid_dto_hasNoViolations() {
        assertNoViolations(validator, validDto());
    }

    @Test
    void id_null_failsValidation() {
        UserDto dto = validDto().setId(null);
        assertFieldHasViolation(validator, dto, "id");
    }

    @Test
    void username_null_failsValidation() {
        UserDto dto = validDto().setUsername(null);
        assertFieldHasViolation(validator, dto, "username");
    }

    @Test
    void username_blank_failsValidation() {
        UserDto dto = validDto().setUsername("");
        assertFieldHasViolation(validator, dto, "username");
    }

    @Test
    void username_exceedsMaxLength_failsValidation() {
        UserDto dto = validDto().setUsername("a".repeat(101));
        assertFieldHasViolation(validator, dto, "username");
    }

    @Test
    void username_exactlyMaxLength_passesValidation() {
        UserDto dto = validDto().setUsername("a".repeat(100));
        assertNoViolations(validator, dto);
    }

    @Test
    void email_null_failsValidation() {
        UserDto dto = validDto().setEmail(null);
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_blank_failsValidation() {
        UserDto dto = validDto().setEmail("");
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_invalidFormat_failsValidation() {
        UserDto dto = validDto().setEmail("not-an-email");
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void email_exceedsMaxLength_failsValidation() {
        UserDto dto = validDto().setEmail("a".repeat(145) + "@x.com");
        assertFieldHasViolation(validator, dto, "email");
    }

    @Test
    void password_null_failsValidation() {
        UserDto dto = validDto().setPassword(null);
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_blank_failsValidation() {
        UserDto dto = validDto().setPassword("");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_noUpperCase_failsValidation() {
        UserDto dto = validDto().setPassword("secure1@");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_noDigit_failsValidation() {
        UserDto dto = validDto().setPassword("Secure@@");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_noSpecialChar_failsValidation() {
        UserDto dto = validDto().setPassword("Secure11");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_tooShort_failsValidation() {
        UserDto dto = validDto().setPassword("Se1@");
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void password_exceedsMaxLength_failsValidation() {
        UserDto dto = validDto().setPassword("Secure1@" + "a".repeat(65));
        assertFieldHasViolation(validator, dto, "password");
    }

    @Test
    void status_null_failsValidation() {
        UserDto dto = validDto().setStatus(null);
        assertFieldHasViolation(validator, dto, "status");
    }

    @Test
    void role_null_failsValidation() {
        UserDto dto = validDto().setRole(null);
        assertFieldHasViolation(validator, dto, "role");
    }

    @Test
    void locations_null_failsValidation() {
        UserDto dto = validDto().setLocations(null);
        assertFieldHasViolation(validator, dto, "locations");
    }

    @Test
    void locations_empty_passesValidation() {
        UserDto dto = validDto().setLocations(List.of());
        assertNoViolations(validator, dto);
    }

    @Test
    void createdAt_null_failsValidation() {
        UserDto dto = validDto().setCreatedAt(null);
        assertFieldHasViolation(validator, dto, "createdAt");
    }

}
