package com.weatherviewer.ratelimit;

import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.time.Duration;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Bucket4jRedisRateLimiterTest {

    @Mock
    private ProxyManager<byte[]> proxyManager;

    @Mock
    private RemoteBucketBuilder<byte[]> remoteBucketBuilder;

    @Mock
    private BucketProxy bucket;

    private Bucket4jRedisRateLimiter rateLimiter;

    private void stubBucket() {
        when(proxyManager.builder()).thenReturn(remoteBucketBuilder);
        when(remoteBucketBuilder.build(any(byte[].class), any(Supplier.class))).thenReturn(bucket);
        rateLimiter = new Bucket4jRedisRateLimiter(proxyManager);
    }

    @Test
    void tryConsume_underLimit_isAllowed() {
        stubBucket();
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.consumed(9, Duration.ofSeconds(60).toNanos()));

        Bucket4jRedisRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.allowed()).isTrue();
    }

    @Test
    void tryConsume_underLimit_reportsCorrectRemaining() {
        stubBucket();
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.consumed(6, Duration.ofSeconds(60).toNanos()));

        Bucket4jRedisRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.remainingRequests()).isEqualTo(6);
    }

    @Test
    void tryConsume_atExactLimit_lastTokenIsStillAllowed() {
        stubBucket();
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.consumed(0, Duration.ofSeconds(60).toNanos()));

        Bucket4jRedisRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingRequests()).isEqualTo(0);
    }

    @Test
    void tryConsume_overLimit_isBlocked() {
        stubBucket();
        when(bucket.tryConsumeAndReturnRemaining(1))
                .thenReturn(ConsumptionProbe.rejected(0, Duration.ofSeconds(42).toNanos(), Duration.ofSeconds(42).toNanos()));

        Bucket4jRedisRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.allowed()).isFalse();
    }

    @Test
    void tryConsume_overLimit_remainingIsZero() {
        stubBucket();
        when(bucket.tryConsumeAndReturnRemaining(1))
                .thenReturn(ConsumptionProbe.rejected(0, Duration.ofSeconds(42).toNanos(), Duration.ofSeconds(42).toNanos()));

        Bucket4jRedisRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.remainingRequests()).isEqualTo(0);
    }

    @Test
    void tryConsume_blocked_reportsNanosToWaitForRefillAsRetryAfterSeconds() {
        stubBucket();
        when(bucket.tryConsumeAndReturnRemaining(1))
                .thenReturn(ConsumptionProbe.rejected(0, Duration.ofSeconds(42).toNanos(), Duration.ofSeconds(42).toNanos()));

        Bucket4jRedisRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.retryAfterSeconds()).isEqualTo(42);
    }

    @Test
    void tryConsume_blocked_roundsSubSecondWaitUpToOneSecond() {
        stubBucket();
        when(bucket.tryConsumeAndReturnRemaining(1))
                .thenReturn(ConsumptionProbe.rejected(0, Duration.ofMillis(200).toNanos(), Duration.ofMillis(200).toNanos()));

        Bucket4jRedisRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.retryAfterSeconds()).isEqualTo(1);
    }

    @Test
    void tryConsume_allowed_retryAfter_convertsSecondsToDuration() {
        stubBucket();
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.consumed(9, Duration.ofSeconds(60).toNanos()));

        Bucket4jRedisRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.retryAfter()).isEqualTo(Duration.ZERO);
    }

    @Test
    void tryConsume_blocked_retryAfter_convertsSecondsToDuration() {
        stubBucket();
        when(bucket.tryConsumeAndReturnRemaining(1))
                .thenReturn(ConsumptionProbe.rejected(0, Duration.ofSeconds(42).toNanos(), Duration.ofSeconds(42).toNanos()));

        Bucket4jRedisRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.retryAfter().getSeconds()).isEqualTo(42);
    }

    @Test
    void tryConsume_redisConnectionFailure_failsOpenAndAllowsRequest() {
        stubBucket();
        when(bucket.tryConsumeAndReturnRemaining(1)).thenThrow(new RedisConnectionFailureException("connection refused"));

        Bucket4jRedisRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.allowed()).isTrue();
    }

    @Test
    void tryConsume_redisConnectionFailure_reportsConfiguredLimitAsRemaining() {
        stubBucket();
        when(bucket.tryConsumeAndReturnRemaining(1)).thenThrow(new RedisConnectionFailureException("connection refused"));

        Bucket4jRedisRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.remainingRequests()).isEqualTo(10);
    }

    @Test
    void tryConsume_unexpectedException_doesNotPropagate() {
        stubBucket();
        when(bucket.tryConsumeAndReturnRemaining(1)).thenThrow(new RuntimeException("boom"));

        assertThat(rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60)).isNotNull();
    }

    @Test
    void tryConsume_buildingBucketThrows_failsOpenAndAllowsRequest() {
        when(proxyManager.builder()).thenThrow(new RedisConnectionFailureException("connection refused"));
        rateLimiter = new Bucket4jRedisRateLimiter(proxyManager);

        Bucket4jRedisRateLimiter.RateLimitResult result = rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 5, 30);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingRequests()).isEqualTo(5);
    }

    @Test
    void tryConsume_usesRedisKeyBytesAsBucketKey() {
        stubBucket();
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(ConsumptionProbe.consumed(9, Duration.ofSeconds(60).toNanos()));

        rateLimiter.tryConsume("rate-limit:/sign-in:ip:203.0.113.5", 10, 60);

        verify(remoteBucketBuilder)
                .build(org.mockito.ArgumentMatchers.eq("rate-limit:/sign-in:ip:203.0.113.5".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                        any(Supplier.class));
    }

}
