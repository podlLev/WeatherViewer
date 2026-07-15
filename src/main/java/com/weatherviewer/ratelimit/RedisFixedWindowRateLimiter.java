package com.weatherviewer.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Fixed-window rate limiter backed by a single atomic Redis Lua script
 * (increment-and-expire-if-new), so concurrent requests across multiple
 * app instances share one accurate counter per key.
 * <p>
 * Fails open: if Redis is unreachable, requests are allowed through rather
 * than blocked, so a Redis outage degrades to "no rate limiting" instead
 * of taking the app down.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisFixedWindowRateLimiter {

    private static final String INCR_AND_EXPIRE_IF_NEW_SCRIPT =
            "local current = redis.call('INCR', KEYS[1]) " +
                    "if current == 1 then " +
                    "  redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
                    "end " +
                    "local ttl = redis.call('TTL', KEYS[1]) " +
                    "return {current, ttl}";

    private final StringRedisTemplate redisTemplate;

    private final DefaultRedisScript<List> incrAndExpireScript = buildScript();

    private static DefaultRedisScript<List> buildScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(INCR_AND_EXPIRE_IF_NEW_SCRIPT);
        script.setResultType(List.class);
        return script;
    }

    /**
     * Attempts to consume one request against the fixed window for
     * {@code redisKey}, atomically incrementing the counter and (on the
     * first hit in a new window) setting it to expire after
     * {@code windowSeconds}.
     *
     * @param redisKey      the counter key, unique per client + rule
     * @param limit         max requests allowed within the window
     * @param windowSeconds window length in seconds
     * @return the outcome: whether the request is allowed, requests
     *         remaining, and seconds until the window resets. If Redis is
     *         unreachable, returns an "allowed" result so requests aren't
     *         blocked by an infrastructure outage.
     */
    public RateLimitResult tryConsume(String redisKey, int limit, int windowSeconds) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> scriptResult = redisTemplate.execute(
                    incrAndExpireScript,
                    List.of(redisKey),
                    String.valueOf(windowSeconds)
            );

            long currentCount = scriptResult == null || scriptResult.isEmpty() ? 0L : scriptResult.get(0);
            long ttlSeconds = scriptResult == null || scriptResult.size() < 2 || scriptResult.get(1) < 0
                    ? windowSeconds
                    : scriptResult.get(1);

            boolean allowed = currentCount <= limit;
            long remaining = Math.max(limit - currentCount, 0);

            return new RateLimitResult(allowed, remaining, ttlSeconds);
        } catch (Exception ex) {
            log.warn("Rate limiter could not reach Redis, allowing request through: {}", ex.getMessage());
            return new RateLimitResult(true, limit, windowSeconds);
        }
    }

    /**
     * Outcome of a {@link #tryConsume} call.
     *
     * @param allowed            whether this request is within the limit
     * @param remainingRequests  requests left in the current window
     * @param retryAfterSeconds  seconds until the window resets
     */
    public record RateLimitResult(boolean allowed, long remainingRequests, long retryAfterSeconds) {

        /** {@link #retryAfterSeconds} as a {@link Duration}, for use with {@code Retry-After} headers. */
        public Duration retryAfter() {
            return Duration.ofSeconds(retryAfterSeconds);
        }

    }

}
