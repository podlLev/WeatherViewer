package com.weatherviewer.validation.validator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();

    @Test
    void null_returnsTrue() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void blank_returnsTrue() {
        assertThat(validator.isValid("", null)).isTrue();
    }

    @Test
    void whitespaceOnly_returnsTrue() {
        assertThat(validator.isValid("   ", null)).isTrue();
    }

    @Test
    void validPassword_returnsTrue() {
        assertThat(validator.isValid("Secure1@", null)).isTrue();
    }

    @Test
    void noUpperCase_returnsFalse() {
        assertThat(validator.isValid("secure1@", null)).isFalse();
    }

    @Test
    void noLowerCase_returnsFalse() {
        assertThat(validator.isValid("SECURE1@", null)).isFalse();
    }

    @Test
    void noDigit_returnsFalse() {
        assertThat(validator.isValid("Secure@@", null)).isFalse();
    }

    @Test
    void noSpecialChar_returnsFalse() {
        assertThat(validator.isValid("Secure11", null)).isFalse();
    }

    @Test
    void tooShort_returnsFalse() {
        assertThat(validator.isValid("Se1@", null)).isFalse();
    }

    @Test
    void exactlyMinLength_returnsTrue() {
        assertThat(validator.isValid("Secur1@a", null)).isTrue();
    }

    @Test
    void withSpaces_returnsFalse() {
        assertThat(validator.isValid("Secure 1@", null)).isFalse();
    }

}
