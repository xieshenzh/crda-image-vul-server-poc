package org.crda.cache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.camel.component.caffeine.processor.idempotent.CaffeineIdempotentRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CaffeineConfiguration {

    @ConfigProperty(name = "caffeine-cache.idempotent-repository-name")
    String idempotentRepositoryName;

    @Produces
    CaffeineIdempotentRepository caffeineIdempotentRepository() {
        return new CaffeineIdempotentRepository(idempotentRepositoryName);
    }
}
