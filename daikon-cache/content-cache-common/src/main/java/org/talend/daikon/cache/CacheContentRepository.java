package org.talend.daikon.cache;

public interface CacheContentRepository<K, C> {

    C get(K key);

    void put(K key, C content);

}
