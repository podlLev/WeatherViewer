package com.weatherviewer.exception;

/**
 * Thrown when a call to an external HTTP service fails — in practice, the
 * OpenWeatherMap API accessed by
 * {@link com.weatherviewer.service.integration.WeatherApiClient} (network
 * failure, timeout, or a non-2xx response). Translated by
 * {@link ControllerExceptionHandler} into a {@code 503 Service Unavailable}
 * response.
 */
public class ExternalHttpCallException extends RuntimeException {

    /**
     * @param message description of what went wrong calling the external service
     */
    public ExternalHttpCallException(String message) {
        super(message);
    }

}
