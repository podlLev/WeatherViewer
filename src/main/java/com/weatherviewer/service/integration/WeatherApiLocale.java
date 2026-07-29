package com.weatherviewer.service.integration;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Set;

/**
 * Maps the current request's locale to a language code OpenWeatherMap
 * accepts for its {@code lang} query parameter, controlling the language of
 * weather {@code description} text in the API response.
 * <p>
 * Used by both {@link WeatherApiClient} (to build the actual request) and
 * {@link WeatherApiCache} (to key cached responses per resolved language —
 * without that, a location's weather cached for one language would be
 * served as-is to a request in another language, since the raw JSON,
 * descriptions included, is what's cached).
 */
public final class WeatherApiLocale {

    /**
     * Languages we ask OpenWeatherMap to translate descriptions into.
     * Keep in sync with the {@code messages_XX.properties} bundles this
     * application ships — no point requesting a language the UI itself
     * can't render around.
     */
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "uk");

    private static final String DEFAULT_LANGUAGE = "en";

    private WeatherApiLocale() {
    }

    /**
     * @return the current {@link LocaleContextHolder} locale's language,
     * if OpenWeatherMap is known to support it, otherwise {@code "en"}.
     */
    public static String resolve() {
        String language = LocaleContextHolder.getLocale().getLanguage();
        return SUPPORTED_LANGUAGES.contains(language) ? language : DEFAULT_LANGUAGE;
    }

}
