package com.weatherviewer.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvalidTokenExceptionTest {

    @Test
    void constructor_setsMessage() {
        InvalidTokenException ex = new InvalidTokenException("This link is invalid.");
        assertThat(ex.getMessage()).isEqualTo("This link is invalid.");
    }

    @Test
    void isInstanceOf_RuntimeException() {
        InvalidTokenException ex = new InvalidTokenException("This link is invalid.");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void thrown_caughtAsRuntimeException() {
        assertThatThrownBy(() -> { throw new InvalidTokenException("This link has expired or was already used."); })
                .isInstanceOf(RuntimeException.class)
                .hasMessage("This link has expired or was already used.");
    }

}
