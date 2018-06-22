package org.talend.daikon.cache.redis;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.cache.RedisCacheElement;
import org.springframework.data.redis.cache.RedisCacheKey;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis cache implementation without lock mechanism.
 *
 * Only 2 operations {@link #get(Object, Callable)} and {@link #get(Object)} are allowed.
 */
public class CloudRedisCache extends NoOpRedisCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudRedisCache.class);

    private final byte[] keyPrefix;

    private final RedisOperations<?, ?> redisOperations;

    private final long expiration;

    private final String name;

    private RedisSerializer<Object> serializer = new JdkSerializationRedisSerializer();

    CloudRedisCache(String name, byte[] keyPrefix, RedisOperations<?, ?> redisOperations, long expiration) {
        super(name, keyPrefix, redisOperations, expiration);
        this.name = name;
        this.keyPrefix = keyPrefix;
        this.redisOperations = redisOperations;
        this.expiration = expiration;
    }

    public Object getNativeCache() {
        return redisOperations;
    }

    public String getName() {
        return this.name;
    }

    RedisCacheKey getCacheKey(Object key) {
        return new RedisCacheKey(key).usePrefix(this.keyPrefix).withKeySerializer(redisOperations.getKeySerializer());
    }

    /**
     * Get the cache entry if found for the given key, or set the cache entry from the provided valueLoader
     */
    @Override
    public <T> T get(final Object key, final Callable<T> valueLoader) {
        Cache.ValueWrapper val = get(key);
        if (val != null) { // return the cache entry if found
            LOGGER.debug("cache value was found for key " + key);
            return (T) val.get();
        }

        LOGGER.debug("cache value was not found for key " + key);
        // put a cache entry if not found
        RedisCacheElement element = new RedisCacheElement(getCacheKey(key), valueLoader).expireAfter(expiration);
        byte[] result = redisOperations.execute((RedisCallback<byte[]>) cnx -> put(cnx, element));
        return (T) (result == null ? null : serializer.deserialize(result));
    }

    /**
     * Get the cache entry from the given key.
     */
    @Override
    public Cache.ValueWrapper get(Object key) {
        RedisCacheKey cacheKey = getCacheKey(key);
        byte[] found = redisOperations.execute((RedisCallback<byte[]>) cnx -> cnx.get(cacheKey.getKeyBytes()));
        return found == null ? null : new RedisCacheElement(cacheKey, serializer.deserialize(found));
    }

    private byte[] put(RedisConnection connection, RedisCacheElement element) throws DataAccessException {
        byte[] value;
        try {
            value = serializer.serialize(((Callable<?>) element.get()).call());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        connection.watch(element.getKeyBytes()); // optimistic locking on the key
        connection.multi(); // start transaction to ensure set and expire actions are an atomic execution
        connection.set(element.getKeyBytes(), value);
        if (!element.isEternal()) {
            connection.expire(element.getKeyBytes(), element.getTimeToLive());
        }
        connection.exec(); // commit the transaction
        return value;
    }
}
