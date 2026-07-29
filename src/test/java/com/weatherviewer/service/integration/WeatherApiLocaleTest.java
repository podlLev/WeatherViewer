package com.weatherviewer.service.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherApiLocaleTest {

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void resolve_english_returnsEn() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        assertThat(WeatherApiLocale.resolve()).isEqualTo("en");
    }

    @Test
    void resolve_ukrainian_returnsUk() {
        LocaleContextHolder.setLocale(new Locale("uk"));
        assertThat(WeatherApiLocale.resolve()).isEqualTo("uk");
    }

    @Test
    void resolve_ukrainianRegionVariant_returnsUk() {
        LocaleContextHolder.setLocale(new Locale("uk", "UA"));
        assertThat(WeatherApiLocale.resolve()).isEqualTo("uk");
    }

    @Test
    void resolve_unsupportedLanguage_fallsBackToEn() {
        LocaleContextHolder.setLocale(Locale.GERMAN);
        assertThat(WeatherApiLocale.resolve()).isEqualTo("en");
    }

}
