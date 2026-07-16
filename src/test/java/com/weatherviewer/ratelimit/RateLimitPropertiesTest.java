package com.weatherviewer.ratelimit;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitPropertiesTest {

    @Test
    void defaults_enabledIsTrue() {
        RateLimitProperties properties = new RateLimitProperties();

        assertThat(properties.isEnabled()).isTrue();
    }

    @Test
    void defaults_defaultLimitIsOneHundred() {
        RateLimitProperties properties = new RateLimitProperties();

        assertThat(properties.getDefaultLimit()).isEqualTo(100);
    }

    @Test
    void defaults_defaultWindowSecondsIsSixty() {
        RateLimitProperties properties = new RateLimitProperties();

        assertThat(properties.getDefaultWindowSeconds()).isEqualTo(60);
    }

    @Test
    void defaults_rulesIsEmptyButNotNull() {
        RateLimitProperties properties = new RateLimitProperties();

        assertThat(properties.getRules()).isNotNull().isEmpty();
    }

    @Test
    void defaults_trustedProxiesIsEmptyButNotNull() {
        RateLimitProperties properties = new RateLimitProperties();

        assertThat(properties.getTrustedProxies()).isNotNull().isEmpty();
    }

    @Test
    void setters_overrideDefaults() {
        RateLimitProperties properties = new RateLimitProperties();

        properties.setEnabled(false);
        properties.setDefaultLimit(10);
        properties.setDefaultWindowSeconds(30);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getDefaultLimit()).isEqualTo(10);
        assertThat(properties.getDefaultWindowSeconds()).isEqualTo(30);
    }

    @Test
    void setRules_storesProvidedRules() {
        RateLimitProperties properties = new RateLimitProperties();
        RateLimitProperties.Rule rule = new RateLimitProperties.Rule();
        rule.setPathPrefix("/sign-in");
        rule.setLimit(10);
        rule.setWindowSeconds(60);

        properties.setRules(List.of(rule));

        assertThat(properties.getRules()).hasSize(1);
        assertThat(properties.getRules().get(0).getPathPrefix()).isEqualTo("/sign-in");
        assertThat(properties.getRules().get(0).getLimit()).isEqualTo(10);
        assertThat(properties.getRules().get(0).getWindowSeconds()).isEqualTo(60);
    }

    @Test
    void setTrustedProxies_storesProvidedValues() {
        RateLimitProperties properties = new RateLimitProperties();

        properties.setTrustedProxies(List.of("10.0.0.0/8", "127.0.0.1"));

        assertThat(properties.getTrustedProxies())
                .hasSize(2)
                .containsExactly("10.0.0.0/8", "127.0.0.1");
    }

    @Test
    void setTrustedProxies_emptyListIsAllowed() {
        RateLimitProperties properties = new RateLimitProperties();

        properties.setTrustedProxies(List.of());

        assertThat(properties.getTrustedProxies()).isEmpty();
    }

    @Test
    void rule_settersAndGetters_roundTrip() {
        RateLimitProperties.Rule rule = new RateLimitProperties.Rule();

        rule.setPathPrefix("/api/v1/weather");
        rule.setLimit(30);
        rule.setWindowSeconds(60);

        assertThat(rule.getPathPrefix()).isEqualTo("/api/v1/weather");
        assertThat(rule.getLimit()).isEqualTo(30);
        assertThat(rule.getWindowSeconds()).isEqualTo(60);
    }

}
