package com.weatherviewer.service.integration;

import com.weatherviewer.exception.ExternalHttpCallException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherApiRetryPredicateTest {

    private final WeatherApiRetryPredicate predicate = new WeatherApiRetryPredicate();

    @Test
    void test_retryableExternalHttpCallException_returnsTrue() {
        assertThat(predicate.test(new ExternalHttpCallException("5xx from provider", true))).isTrue();
    }

    @Test
    void test_nonRetryableExternalHttpCallException_returnsFalse() {
        assertThat(predicate.test(new ExternalHttpCallException("404 unknown city", false))).isFalse();
    }

    @Test
    void test_otherExceptionTypes_defaultsToRetryable() {
        assertThat(predicate.test(new IOException("connection reset"))).isTrue();
    }

}
