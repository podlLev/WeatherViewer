package com.weatherviewer.service.integration;

import com.weatherviewer.exception.ExternalHttpCallException;

import java.util.function.Predicate;

/**
 * Decides, for the {@code weatherApi} resilience4j retry instance
 * (configured in {@code application.properties} via
 * {@code resilience4j.retry.instances.weatherApi.retry-exception-predicate}),
 * whether a failure from {@link WeatherApiClient} is worth retrying.
 * <p>
 * Transient failures — network errors, timeouts, malformed responses, and
 * 5xx status codes — are retried. Client errors (bad request, invalid API
 * key, unknown city) are marked non-retryable on the exception itself by
 * {@link WeatherApiClient}, since retrying an inherently-wrong request
 * three times only adds latency and burns the rate-limited provider quota
 * without any chance of succeeding.
 */
public class WeatherApiRetryPredicate implements Predicate<Throwable> {

    @Override
    public boolean test(Throwable throwable) {
        if (throwable instanceof ExternalHttpCallException externalHttpCallException) {
            return externalHttpCallException.isRetryable();
        }
        return true;
    }

}
