package com.weatherviewer.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalHttpCallExceptionTest {

    @Test
    void constructor_setsMessage() {
        ExternalHttpCallException ex = new ExternalHttpCallException("External call failed");
        assertThat(ex.getMessage()).isEqualTo("External call failed");
    }

    @Test
    void isInstanceOf_RuntimeException() {
        ExternalHttpCallException ex = new ExternalHttpCallException("External call failed");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void thrown_canBeCaughtAsRuntimeException() {
        assertThatThrownBy(() -> { throw new ExternalHttpCallException("External call failed"); })
                .isInstanceOf(RuntimeException.class)
                .hasMessage("External call failed");
    }

}
