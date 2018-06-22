package org.talend.daikon.cache;

import java.util.concurrent.Callable;

import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

@Component
public class SpringCacheContentRepository<K, C> implements CacheContentRepository<K, C> {

    private Cache cache;

    private Callable<C> fetchIfMissing;

    public SpringCacheContentRepository(Cache cache, Callable<C> fetchIfMissing) {
        this.cache = cache;
        this.fetchIfMissing = fetchIfMissing;
    }

    @Override
    public C get(K key) {
        return cache.get(key, fetchIfMissing);
    }

    @Override
    public void put(K key, C content) {
        cache.put(key, content);
    }

}
