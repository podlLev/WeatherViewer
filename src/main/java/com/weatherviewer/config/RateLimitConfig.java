package com.weatherviewer.config;

import com.weatherviewer.ratelimit.Bucket4jRedisRateLimiter;
import com.weatherviewer.ratelimit.RateLimitProperties;
import com.weatherviewer.ratelimit.RateLimitingFilter;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(prefix = "rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class RateLimitConfig {

    private final RateLimitProperties rateLimitProperties;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean(destroyMethod = "shutdown")
    public RedisClient rateLimitRedisClient() {
        return RedisClient.create(RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .build());
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<byte[], byte[]> rateLimitRedisConnection(RedisClient rateLimitRedisClient) {
        return rateLimitRedisClient.connect(ByteArrayCodec.INSTANCE);
    }

    @Bean
    public LettuceBasedProxyManager<byte[]> rateLimitProxyManager(
            StatefulRedisConnection<byte[], byte[]> rateLimitRedisConnection) {
        ClientSideConfig clientSideConfig = ClientSideConfig.getDefault()
                .withExpirationAfterWriteStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(10))
                );

        return LettuceBasedProxyManager.builderFor(rateLimitRedisConnection)
                .withClientSideConfig(clientSideConfig)
                .build();
    }

    @Bean
    public RateLimitingFilter rateLimitingFilter(Bucket4jRedisRateLimiter rateLimiter) {
        return new RateLimitingFilter(rateLimiter, rateLimitProperties);
    }

    @Bean
    public Bucket4jRedisRateLimiter bucket4jRedisRateLimiter(LettuceBasedProxyManager<byte[]> rateLimitProxyManager) {
        return new Bucket4jRedisRateLimiter(rateLimitProxyManager);
    }

}
