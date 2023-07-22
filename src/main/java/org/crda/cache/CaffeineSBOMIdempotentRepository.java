package org.crda.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.crda.sbom.SBOMIdempotentRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class CaffeineSBOMIdempotentRepository extends CaffeineIdempotentRepository implements SBOMIdempotentRepository {

    @ConfigProperty(name = "caffeine-cache.sbom.expire-after-write-time", defaultValue = "300")
    long expireAfterWriteTime;

    @ConfigProperty(name = "caffeine-cache.sbom.expire-after-access-time", defaultValue = "300")
    long expireAfterAccessTime;

    static final String sbomsCacheName = "sbomsIdem";

    @Inject
    public CaffeineSBOMIdempotentRepository() {
        super(sbomsCacheName);
    }

    @Override
    Caffeine<Object, Object> getCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(expireAfterWriteTime, TimeUnit.SECONDS)
                .expireAfterAccess(expireAfterAccessTime, TimeUnit.SECONDS);
    }
}
