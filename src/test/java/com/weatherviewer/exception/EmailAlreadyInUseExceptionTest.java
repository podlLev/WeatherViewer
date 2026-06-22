package com.weatherviewer.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailAlreadyInUseExceptionTest {

    @Test
    void constructor_setsFormattedMessage() {
        EmailAlreadyInUseException ex = new EmailAlreadyInUseException("test@example.com");
        assertThat(ex.getMessage()).isEqualTo("Email already in use: test@example.com");
    }

    @Test
    void isInstanceOf_RuntimeException() {
        EmailAlreadyInUseException ex = new EmailAlreadyInUseException("test@example.com");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void thrown_caughtAsRuntimeException() {
        assertThatThrownBy(() -> { throw new EmailAlreadyInUseException("test@example.com"); })
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already in use: test@example.com");
    }

}
