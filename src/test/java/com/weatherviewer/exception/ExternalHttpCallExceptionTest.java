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

    @Test
    void singleArgConstructor_defaultsToRetryable() {
        ExternalHttpCallException ex = new ExternalHttpCallException("Timeout");
        assertThat(ex.isRetryable()).isTrue();
    }

    @Test
    void twoArgConstructor_setsRetryableFlag() {
        ExternalHttpCallException retryable = new ExternalHttpCallException("5xx from provider", true);
        ExternalHttpCallException notRetryable = new ExternalHttpCallException("404 unknown city", false);

        assertThat(retryable.isRetryable()).isTrue();
        assertThat(notRetryable.isRetryable()).isFalse();
    }

}
