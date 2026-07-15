package com.weatherviewer.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the {@link RateLimitingFilter}, bound from properties
 * under the {@code rate-limit} prefix (see {@code application.properties}).
 * <p>
 * {@link #defaultLimit}/{@link #defaultWindowSeconds} apply to any request
 * path that doesn't match one of the more specific {@link #rules}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    /** Master on/off switch for rate limiting. */
    private boolean enabled = true;

    /** Requests allowed per window for paths with no matching rule. */
    private int defaultLimit = 100;

    /** Window length, in seconds, for the default limit. */
    private int defaultWindowSeconds = 60;

    /** Path-prefix-specific overrides, checked longest-prefix-first. */
    private List<Rule> rules = new ArrayList<>();

    /** A single rate limit rule scoped to requests under {@link #pathPrefix}. */
    @Getter
    @Setter
    public static class Rule {

        /** URI prefix this rule applies to, e.g. {@code "/api/v1/weather"}. */
        private String pathPrefix;

        /** Requests allowed per window for matching paths. */
        private int limit;

        /** Window length, in seconds, for this rule's limit. */
        private int windowSeconds;

    }

}
