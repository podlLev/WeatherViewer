package com.weatherviewer.config;

import com.weatherviewer.ratelimit.RateLimitProperties;
import com.weatherviewer.ratelimit.RateLimitingFilter;
import com.weatherviewer.ratelimit.RedisFixedWindowRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@RequiredArgsConstructor
public class RateLimitConfig {

    private final RateLimitProperties rateLimitProperties;
    private final RedisFixedWindowRateLimiter redisFixedWindowRateLimiter;

    @Bean
    public RateLimitingFilter rateLimitingFilter() {
        return new RateLimitingFilter(redisFixedWindowRateLimiter, rateLimitProperties);
    }

}
