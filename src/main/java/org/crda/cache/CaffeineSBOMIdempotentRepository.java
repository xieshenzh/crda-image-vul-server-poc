package org.crda.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.crda.sbom.SBOMIdempotentRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CaffeineSBOMIdempotentRepository extends CaffeineIdempotentRepository implements SBOMIdempotentRepository {

    @ConfigProperty(name = "camel.component.caffeine-cache.initial-capacity", defaultValue = "10000")
    int initialCapacity;

    @ConfigProperty(name = "camel.component.caffeine-cache.maximum-size", defaultValue = "10000")
    long maximumSize;

    @Inject
    public CaffeineSBOMIdempotentRepository(@ConfigProperty(name = "caffeine-cache.SBOM-idempotent-repository-name", defaultValue = "sbomsIdem") String cacheName) {
        super(cacheName);
    }

    @Override
    Caffeine<Object, Object> getCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(initialCapacity)
                .maximumSize(maximumSize);
    }
}
