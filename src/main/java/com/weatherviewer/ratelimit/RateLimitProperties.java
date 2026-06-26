package com.weatherviewer.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private int defaultLimit = 100;

    private int defaultWindowSeconds = 60;

    private List<Rule> rules = new ArrayList<>();

    @Getter
    @Setter
    public static class Rule {

        private String pathPrefix;

        private int limit;

        private int windowSeconds;

    }

}
