package com.weatherviewer.ratelimit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisFixedWindowRateLimiterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private RedisFixedWindowRateLimiter rateLimiter;

    private void stubScriptResult(long count, long ttl) {
        when(redisTemplate.execute(any(), anyList(), any()))
                .thenReturn(List.of(count, ttl));
    }

    @Test
    void tryConsume_underLimit_isAllowed() {
        stubScriptResult(1L, 60L);

        RedisFixedWindowRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.allowed()).isTrue();
    }

    @Test
    void tryConsume_underLimit_reportsCorrectRemaining() {
        stubScriptResult(4L, 60L);

        RedisFixedWindowRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.remainingRequests()).isEqualTo(6);
    }

    @Test
    void tryConsume_atExactLimit_isStillAllowed() {
        stubScriptResult(10L, 60L);

        RedisFixedWindowRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingRequests()).isEqualTo(0);
    }

    @Test
    void tryConsume_overLimit_isBlocked() {
        stubScriptResult(11L, 60L);

        RedisFixedWindowRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.allowed()).isFalse();
    }

    @Test
    void tryConsume_overLimit_remainingNeverGoesNegative() {
        stubScriptResult(25L, 60L);

        RedisFixedWindowRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.remainingRequests()).isEqualTo(0);
    }

    @Test
    void tryConsume_returnsTtlFromScript_asRetryAfterSeconds() {
        stubScriptResult(11L, 42L);

        RedisFixedWindowRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.retryAfterSeconds()).isEqualTo(42);
    }

    @Test
    void tryConsume_retryAfter_convertsSecondsToDuration() {
        stubScriptResult(11L, 42L);

        RedisFixedWindowRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.retryAfter().getSeconds()).isEqualTo(42);
    }

    @Test
    void tryConsume_nullScriptResult_treatedAsFirstHitAndAllowed() {
        when(redisTemplate.execute(any(), anyList(), any())).thenReturn(null);

        RedisFixedWindowRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.allowed()).isTrue();
    }

    @Test
    void tryConsume_negativeTtlFromScript_fallsBackToConfiguredWindow() {
        when(redisTemplate.execute(any(), anyList(), any()))
                .thenReturn(List.of(1L, -1L));

        RedisFixedWindowRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.retryAfterSeconds()).isEqualTo(60);
    }

    @Test
    void tryConsume_redisThrows_failsOpenAndAllowsRequest() {
        when(redisTemplate.execute(any(), anyList(), any()))
                .thenThrow(new RedisConnectionFailureException("connection refused"));

        RedisFixedWindowRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.allowed()).isTrue();
    }

    @Test
    void tryConsume_redisThrows_reportsConfiguredLimitAsRemaining() {
        when(redisTemplate.execute(any(), anyList(), any()))
                .thenThrow(new RedisConnectionFailureException("connection refused"));

        RedisFixedWindowRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.remainingRequests()).isEqualTo(10);
    }

    @Test
    void tryConsume_redisThrows_doesNotPropagateException() {
        when(redisTemplate.execute(any(), anyList(), any()))
                .thenThrow(new RuntimeException("boom"));

        assertThat(rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60)).isNotNull();
    }

    @Test
    void tryConsume_emptyScriptResult_treatedAsFirstHitAndAllowed() {
        when(redisTemplate.execute(any(), anyList(), any()))
                .thenReturn(List.of());

        RedisFixedWindowRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingRequests()).isEqualTo(10);
        assertThat(result.retryAfterSeconds()).isEqualTo(60);
    }

    @Test
    void tryConsume_scriptReturnsOnlyCounter_usesConfiguredWindowAsRetryAfter() {
        when(redisTemplate.execute(any(), anyList(), any()))
                .thenReturn(List.of(5L));

        RedisFixedWindowRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingRequests()).isEqualTo(5);
        assertThat(result.retryAfterSeconds()).isEqualTo(60);
    }

}
