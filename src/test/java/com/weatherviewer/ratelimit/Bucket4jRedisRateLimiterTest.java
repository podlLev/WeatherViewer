package com.weatherviewer.ratelimit;

import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Bucket4jRedisRateLimiterTest {

    @Mock
    private LettuceBasedProxyManager<byte[]> proxyManager;

    @Mock
    private RemoteBucketBuilder<byte[]> bucketBuilder;

    @Mock
    private BucketProxy bucket;

    @InjectMocks
    private Bucket4jRedisRateLimiter rateLimiter;

    @SuppressWarnings("unchecked")
    private void stubBucket(boolean consumed, long remaining, long nanosToWait) {
        when(proxyManager.builder()).thenReturn(bucketBuilder);
        when(bucketBuilder.build(any(byte[].class), any(Supplier.class))).thenReturn(bucket);
        ConsumptionProbe probe = consumed
                ? ConsumptionProbe.consumed(remaining, nanosToWait)
                : ConsumptionProbe.rejected(remaining, nanosToWait, nanosToWait);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
    }

    @Test
    void tryConsume_underLimit_isAllowed() {
        stubBucket(true, 5, 0);

        Bucket4jRedisRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingRequests()).isEqualTo(5);
    }

    @Test
    void tryConsume_overLimit_isBlocked() {
        stubBucket(false, 0, 30_000_000_000L);

        Bucket4jRedisRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.allowed()).isFalse();
        assertThat(result.retryAfterSeconds()).isGreaterThan(0);
    }

    @Test
    void tryConsume_overLimit_remainingNeverGoesNegative() {
        stubBucket(false, -1, 10_000_000_000L);

        Bucket4jRedisRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.remainingRequests()).isEqualTo(0);
    }

    @Test
    void tryConsume_retryAfter_convertsSecondsToDuration() {
        stubBucket(false, 0, 42_000_000_000L);

        Bucket4jRedisRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.retryAfter().getSeconds()).isEqualTo(result.retryAfterSeconds());
    }

    @Test
    void tryConsume_proxyManagerThrows_failsOpenAndAllowsRequest() {
        when(proxyManager.builder()).thenThrow(new RuntimeException("connection refused"));

        Bucket4jRedisRateLimiter.RateLimitResult result =
                rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingRequests()).isEqualTo(10);
    }

    @Test
    void tryConsume_proxyManagerThrows_doesNotPropagateException() {
        when(proxyManager.builder()).thenThrow(new RuntimeException("boom"));

        assertThat(rateLimiter.tryConsume("rate-limit:test:ip:1.2.3.4", 10, 60)).isNotNull();
    }

}
