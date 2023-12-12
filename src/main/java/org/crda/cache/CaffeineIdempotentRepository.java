/**
 * Contents in this file are from:
 * https://github.com/apache/camel/blob/72d0c6e5bcad235db00ab8bba07e265b04ae7509/components/camel-caffeine/src/main/java/org/apache/camel/component/caffeine/processor/idempotent/CaffeineIdempotentRepository.java
 */

package org.crda.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.camel.api.management.ManagedAttribute;
import org.apache.camel.api.management.ManagedOperation;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.support.service.ServiceSupport;

@ManagedResource(description = "Caffeine based message id repository")
public abstract class CaffeineIdempotentRepository extends ServiceSupport implements IdempotentRepository {

    String cacheName;

    private Cache<String, Boolean> cache;

    CaffeineIdempotentRepository() {
    }

    public CaffeineIdempotentRepository(String repositoryName) {
        this.cacheName = repositoryName;
    }

    @ManagedAttribute(description = "The processor name")
    public String getCacheName() {
        return cacheName;
    }

    @Override
    @ManagedOperation(description = "Adds the key to the store")
    public boolean add(String key) {
        if (cache.asMap().containsKey(key)) {
            return false;
        } else {
            cache.put(key, true);
            return true;
        }
    }

    @Override
    public boolean confirm(String key) {
        return cache.asMap().containsKey(key);
    }

    @Override
    @ManagedOperation(description = "Does the store contain the given key")
    public boolean contains(String key) {
        return cache.asMap().containsKey(key);
    }

    @Override
    @ManagedOperation(description = "Remove the key from the store")
    public boolean remove(String key) {
        cache.invalidate(key);
        return true;
    }

    @Override
    @ManagedOperation(description = "Clear the store")
    public void clear() {
        cache.invalidateAll();
    }

    @Override
    protected void doStart() throws Exception {
        if (cache == null) {
            Caffeine<Object, Object> builder = this.getCacheBuilder();
            cache = builder.build();
        }
    }

    protected Cache getCache() {
        return this.cache;
    }

    @Override
    protected void doStop() throws Exception {
    }

    protected abstract Caffeine<Object, Object> getCacheBuilder();
}

