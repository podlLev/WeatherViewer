package com.weatherviewer.validation.validator;

import com.weatherviewer.dto.CreateUserDto;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PasswordMatchesValidatorTest {

    private final PasswordMatchesValidator validator = new PasswordMatchesValidator();

    private ConstraintValidatorContext mockContext() {
        return mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS);
    }

    private CreateUserDto dtoWithPasswords(String password, String repeatPassword) {
        return new CreateUserDto()
                .setPassword(password)
                .setRepeatPassword(repeatPassword);
    }

    @Test
    void null_dto_returnsFalse() {
        assertThat(validator.isValid(null, mockContext())).isFalse();
    }

    @Test
    void matchingPasswords_returnsTrue() {
        CreateUserDto dto = dtoWithPasswords("Secure1@", "Secure1@");
        assertThat(validator.isValid(dto, mockContext())).isTrue();
    }

    @Test
    void nonMatchingPasswords_returnsFalse() {
        CreateUserDto dto = dtoWithPasswords("Secure1@", "Different1@");
        assertThat(validator.isValid(dto, mockContext())).isFalse();
    }

    @Test
    void bothPasswordsNull_returnsTrue() {
        CreateUserDto dto = dtoWithPasswords(null, null);
        assertThat(validator.isValid(dto, mockContext())).isTrue();
    }

    @Test
    void onePasswordNull_returnsFalse() {
        CreateUserDto dto = dtoWithPasswords("Secure1@", null);
        assertThat(validator.isValid(dto, mockContext())).isFalse();
    }

}
