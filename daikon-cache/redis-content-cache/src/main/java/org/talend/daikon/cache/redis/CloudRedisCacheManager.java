package org.talend.daikon.cache.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisOperations;

@ConditionalOnProperty(name = "content-service.cache", havingValue = "redis")
public class CloudRedisCacheManager extends RedisCacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudRedisCacheManager.class);

    public CloudRedisCacheManager(RedisOperations redisOperations) {
        super(redisOperations);
    }

    @Override
    protected RedisCache createCache(String cacheName) {
        long expiration = computeExpiration(cacheName);
        CloudRedisCache cloudRedisCache = new CloudRedisCache(cacheName,
                super.isUsePrefix() ? getCachePrefix().prefix(cacheName) : null, super.getRedisOperations(), expiration);
        LOGGER.info("created redis cache with name " + cacheName + " and expiration " + expiration);
        return cloudRedisCache;
    }
}
