package com.weatherviewer.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisFixedWindowRateLimiterTest {

    private static final String KEY = "rate-limit:/api/v1/weather:ip:127.0.0.1";
    private static final int LIMIT = 10;
    private static final int WINDOW_SECONDS = 60;

    @Mock
    private StringRedisTemplate redisTemplate;

    private RedisFixedWindowRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RedisFixedWindowRateLimiter(redisTemplate);
    }

    @SuppressWarnings("unchecked")
    private void stubScriptResult(List<Long> result) {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
                .thenReturn(result);
    }

    @SuppressWarnings("unchecked")
    private void stubScriptThrows(RuntimeException ex) {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any()))
                .thenThrow(ex);
    }

    @Test
    void underLimit_isAllowedAndReportsRemaining() {
        stubScriptResult(List.of(3L, 57L));

        RedisFixedWindowRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(KEY, LIMIT, WINDOW_SECONDS);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingRequests()).isEqualTo(7L);
        assertThat(result.retryAfterSeconds()).isEqualTo(57L);
    }

    @Test
    void exactlyAtLimit_isStillAllowed() {
        stubScriptResult(List.of(10L, 30L));

        RedisFixedWindowRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(KEY, LIMIT, WINDOW_SECONDS);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingRequests()).isEqualTo(0L);
    }

    @Test
    void overLimit_isBlockedWithZeroRemaining() {
        stubScriptResult(List.of(11L, 12L));

        RedisFixedWindowRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(KEY, LIMIT, WINDOW_SECONDS);

        assertThat(result.allowed()).isFalse();
        assertThat(result.remainingRequests()).isEqualTo(0L);
        assertThat(result.retryAfterSeconds()).isEqualTo(12L);
    }

    @Test
    void wellOverLimit_remainingNeverGoesNegative() {
        stubScriptResult(List.of(500L, 5L));

        RedisFixedWindowRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(KEY, LIMIT, WINDOW_SECONDS);

        assertThat(result.remainingRequests()).isEqualTo(0L);
    }

    @Test
    void negativeTtlFromRedis_fallsBackToConfiguredWindow() {
        // TTL can come back as -1 if the key exists without an expiry (e.g. EXPIRE call raced/failed)
        stubScriptResult(List.of(4L, -1L));

        RedisFixedWindowRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(KEY, LIMIT, WINDOW_SECONDS);

        assertThat(result.retryAfterSeconds()).isEqualTo(WINDOW_SECONDS);
    }

    @Test
    void nullScriptResult_treatedAsFirstRequestInWindow() {
        stubScriptResult(null);

        RedisFixedWindowRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(KEY, LIMIT, WINDOW_SECONDS);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingRequests()).isEqualTo(LIMIT);
        assertThat(result.retryAfterSeconds()).isEqualTo(WINDOW_SECONDS);
    }

    @Test
    void emptyScriptResult_treatedAsFirstRequestInWindow() {
        stubScriptResult(Collections.emptyList());

        RedisFixedWindowRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(KEY, LIMIT, WINDOW_SECONDS);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingRequests()).isEqualTo(LIMIT);
        assertThat(result.retryAfterSeconds()).isEqualTo(WINDOW_SECONDS);
    }

    @Test
    void singleElementScriptResult_fallsBackToConfiguredWindowForTtl() {
        stubScriptResult(List.of(2L));

        RedisFixedWindowRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(KEY, LIMIT, WINDOW_SECONDS);

        assertThat(result.remainingRequests()).isEqualTo(LIMIT - 2L);
        assertThat(result.retryAfterSeconds()).isEqualTo(WINDOW_SECONDS);
    }

    @Test
    void redisConnectionFailure_failsOpenAndAllowsRequestWithFullQuota() {
        stubScriptThrows(new RedisConnectionFailureException("Unable to connect to Redis"));

        RedisFixedWindowRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(KEY, LIMIT, WINDOW_SECONDS);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingRequests()).isEqualTo(LIMIT);
        assertThat(result.retryAfterSeconds()).isEqualTo(WINDOW_SECONDS);
    }

    @Test
    void anyRuntimeExceptionFromRedis_failsOpenRatherThanPropagating() {
        stubScriptThrows(new IllegalStateException("NOSCRIPT No matching script. Please use EVAL."));

        RedisFixedWindowRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(KEY, LIMIT, WINDOW_SECONDS);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingRequests()).isEqualTo(LIMIT);
        assertThat(result.retryAfterSeconds()).isEqualTo(WINDOW_SECONDS);
    }

    @Test
    void retryAfter_convertsSecondsToDuration() {
        stubScriptResult(List.of(1L, 42L));

        RedisFixedWindowRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume(KEY, LIMIT, WINDOW_SECONDS);

        assertThat(result.retryAfter().getSeconds()).isEqualTo(42L);
    }

}
