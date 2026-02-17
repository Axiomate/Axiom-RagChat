package com.axiom.RagChat.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Supplier;

@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final RedisClient redisClient;

    @Value("${ragchat.rate-limit.capacity:100}")
    private long capacity;

    @Value("${ragchat.rate-limit.refill-tokens:100}")
    private long refillTokens;

    @Value("${ragchat.rate-limit.refill-duration-minutes:1}")
    private long refillDurationMinutes;

    @Bean
    public ProxyManager<String> proxyManager() {
        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(
            io.lettuce.core.codec.RedisCodec.of(
                io.lettuce.core.codec.StringCodec.UTF8,
                io.lettuce.core.codec.ByteArrayCodec.INSTANCE
            )
        );

        return LettuceBasedProxyManager.builderFor(connection)
            .withExpirationStrategy(
                // Keep Redis keys alive for 2x the refill window so buckets
                // are never evicted mid-window. Adjust multiplier if your
                // refill-duration-minutes is very long.
                ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                    Duration.ofMinutes(refillDurationMinutes * 2)
                )
            )
            .build();
    }

    public Supplier<BucketConfiguration> bucketConfiguration() {
        return () -> {
            Bandwidth limit = Bandwidth.simple(capacity, Duration.ofMinutes(refillDurationMinutes));
            return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
        };
    }

    public Bucket resolveBucket(String key) {
        return proxyManager().builder().build(key, bucketConfiguration());
    }
}