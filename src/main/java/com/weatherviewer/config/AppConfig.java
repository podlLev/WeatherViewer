package com.weatherviewer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * General-purpose application beans that don't belong to a more specific
 * configuration class (HTTP client, security redirect strategy, locale).
 */
@Configuration
public class AppConfig {

    /**
     * HTTP client used by {@link com.weatherviewer.service.integration.WeatherApiClient} to
     * call OpenWeatherMap. Explicit connect/read timeouts are required here: without them the
     * underlying JDK client factory has no default timeout, so a hung or slow OpenWeatherMap
     * response would block the calling request thread indefinitely and, under load, exhaust
     * the server's thread pool.
     */
    @Bean
    public RestClient restClient(
            RestClient.Builder restClientBuilder,
            @Value("${weather.api.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${weather.api.read-timeout-ms:5000}") int readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        return restClientBuilder.requestFactory(requestFactory).build();
    }

    /** Default Spring Security redirect strategy, used by {@link com.weatherviewer.security.CustomAuthSuccessHandler}. */
    @Bean
    public RedirectStrategy redirectStrategy() {
        return new DefaultRedirectStrategy();
    }

    /** Resolves the request locale from the HTTP session, defaulting to English. */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }

    /**
     * Dedicated, bounded thread pool used by
     * {@link com.weatherviewer.controller.HomeController} to fetch weather
     * for a user's saved locations concurrently.
     * <p>
     * Deliberately separate from the JVM-wide common {@code ForkJoinPool}
     * (what a bare {@code parallelStream()} would use): that pool is shared
     * with unrelated parallel streams elsewhere in the JVM and has no
     * request-scoped bound, so a user with many saved locations could
     * starve it for everyone. Sized via {@code weather.dashboard.fetch-pool-size}
     * (default 20) and shut down automatically on context close.
     */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService weatherFetchExecutor(
            @Value("${weather.dashboard.fetch-pool-size:20}") int poolSize) {
        return Executors.newFixedThreadPool(poolSize, new CustomizableThreadFactory("weather-fetch-"));
    }

}
