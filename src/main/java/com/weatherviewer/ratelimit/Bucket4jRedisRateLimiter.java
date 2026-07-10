package com.weatherviewer.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Slf4j
public class Bucket4jRedisRateLimiter {

    private final LettuceBasedProxyManager<byte[]> proxyManager;

    private final ConcurrentHashMap<String, BucketConfiguration> configCache = new ConcurrentHashMap<>();

    public RateLimitResult tryConsume(String key, int limit, int windowSeconds) {
        try {
            BucketConfiguration configuration = configCache.computeIfAbsent(
                    limit + ":" + windowSeconds,
                    ignored -> buildConfiguration(limit, windowSeconds)
            );

            byte[] redisKey = key.getBytes(StandardCharsets.UTF_8);
            Supplier<BucketConfiguration> configurationSupplier = () -> configuration;
            ConsumptionProbe probe = proxyManager.builder()
                    .build(redisKey, configurationSupplier)
                    .tryConsumeAndReturnRemaining(1);

            boolean allowed = probe.isConsumed();
            long remaining = Math.max(probe.getRemainingTokens(), 0);
            long retryAfterSeconds = allowed
                    ? 0
                    : Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1);

            return new RateLimitResult(allowed, remaining, retryAfterSeconds);
        } catch (Exception ex) {
            log.warn("Rate limiter could not reach Redis, allowing request through: {}", ex.getMessage());
            return new RateLimitResult(true, limit, windowSeconds);
        }
    }

    private BucketConfiguration buildConfiguration(int limit, int windowSeconds) {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limit)
                        .refillIntervally(limit, Duration.ofSeconds(windowSeconds))
                        .build())
                .build();
    }

    public record RateLimitResult(boolean allowed, long remainingRequests, long retryAfterSeconds) {
        public Duration retryAfter() {
            return Duration.ofSeconds(retryAfterSeconds);
        }
    }

}
