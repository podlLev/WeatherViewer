package com.weatherviewer.dto;

import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.weatherviewer.dto.helper.ValidatorTestFactory.*;

class UpdateUserRoleDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = skipValidator();
    }

    private UpdateUserRoleDto validDto() {
        return new UpdateUserRoleDto()
                .setUserId(UUID.randomUUID())
                .setNewRole("USER");
    }

    @Test
    void valid_dto_user_hasNoViolations() {
        assertNoViolations(validator, validDto());
    }

    @Test
    void valid_dto_admin_hasNoViolations() {
        UpdateUserRoleDto dto = validDto().setNewRole("ADMIN");
        assertNoViolations(validator, dto);
    }

    @Test
    void userId_null_failsValidation() {
        UpdateUserRoleDto dto = validDto().setUserId(null);
        assertFieldHasViolation(validator, dto, "userId");
    }

    @Test
    void newRole_null_failsValidation() {
        UpdateUserRoleDto dto = validDto().setNewRole(null);
        assertFieldHasViolation(validator, dto, "newRole");
    }

    @Test
    void newRole_blank_failsValidation() {
        UpdateUserRoleDto dto = validDto().setNewRole("");
        assertFieldHasViolation(validator, dto, "newRole");
    }

    @Test
    void newRole_lowercase_failsValidation() {
        UpdateUserRoleDto dto = validDto().setNewRole("user");
        assertFieldHasViolation(validator, dto, "newRole");
    }

    @Test
    void newRole_invalid_failsValidation() {
        UpdateUserRoleDto dto = validDto().setNewRole("MODERATOR");
        assertFieldHasViolation(validator, dto, "newRole");
    }

    @Test
    void newRole_withSpaces_failsValidation() {
        UpdateUserRoleDto dto = validDto().setNewRole("USER ADMIN");
        assertFieldHasViolation(validator, dto, "newRole");
    }

}
