package com.weatherviewer.exception;

import lombok.Getter;

/**
 * Thrown when a call to an external HTTP service fails — in practice, the
 * OpenWeatherMap API accessed by
 * {@link com.weatherviewer.service.integration.WeatherApiClient} (network
 * failure, timeout, or a non-2xx response). Translated by
 * {@link ControllerExceptionHandler} into a {@code 503 Service Unavailable}
 * response.
 */
@Getter
public class ExternalHttpCallException extends RuntimeException {

    /**
     * Whether this failure is worth retrying. Network errors, timeouts, and
     * 5xx responses from the provider are transient and {@code retryable};
     * 4xx responses (bad request, unauthorized, not found) reflect a
     * problem with the request itself and won't succeed on a second try, so
     * they're marked {@code false} and skipped by
     * {@link com.weatherviewer.service.integration.WeatherApiRetryPredicate}.
     */
    private final boolean retryable;

    /**
     * @param message description of what went wrong calling the external service
     */
    public ExternalHttpCallException(String message) {
        this(message, true);
    }

    /**
     * @param message   description of what went wrong calling the external service
     * @param retryable whether a retry might succeed
     */
    public ExternalHttpCallException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

}
