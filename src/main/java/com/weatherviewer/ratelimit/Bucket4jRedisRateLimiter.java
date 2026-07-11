package com.weatherviewer.ratelimit;

import io.github.bucket4j.*;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class Bucket4jRedisRateLimiter {

    private final ProxyManager<byte[]> proxyManager;

    public RateLimitResult tryConsume(String redisKey, int limit, int windowSeconds) {
        try {
            byte[] key = redisKey.getBytes(StandardCharsets.UTF_8);
            Bucket bucket = proxyManager.builder().build(key, () -> bucketConfiguration(limit, windowSeconds));

            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            long retryAfterSeconds = probe.isConsumed()
                    ? 0L
                    : Math.max(1L, Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds());

            return new RateLimitResult(probe.isConsumed(), probe.getRemainingTokens(), retryAfterSeconds);
        } catch (Exception ex) {
            log.warn("Rate limiter could not reach Redis, allowing request through: {}", ex.getMessage());
            return new RateLimitResult(true, limit, windowSeconds);
        }
    }

    private BucketConfiguration bucketConfiguration(int limit, int windowSeconds) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(limit)
                .refillIntervally(limit, Duration.ofSeconds(windowSeconds))
                .build();

        return BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();
    }

    public record RateLimitResult(boolean allowed, long remainingRequests, long retryAfterSeconds) {
        public Duration retryAfter() {
            return Duration.ofSeconds(retryAfterSeconds);
        }
    }

}
