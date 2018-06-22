package org.talend.daikon.cache.redis;

import java.util.concurrent.Callable;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisOperations;

/**
 * Redis cache which forbid all {@link org.springframework.cache.Cache} operations.
 * It forces sub classes to explicitly declare supported operations.
 */
public abstract class NoOpRedisCache extends RedisCache {

    public NoOpRedisCache(String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration) {
        super(name, prefix, redisOperations, expiration);
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getNativeCache() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueWrapper get(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void evict(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
