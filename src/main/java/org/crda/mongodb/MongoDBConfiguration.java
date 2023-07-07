package org.crda.mongodb;

import com.mongodb.client.MongoClient;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.apache.camel.component.mongodb.processor.idempotent.MongoDbIdempotentRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public class MongoDBConfiguration {

    @Inject
    MongoClient mongoClient;

    @ConfigProperty(name = "quarkus.mongodb.crda.database")
    String dbName;

    @ConfigProperty(name = "quarkus.mongodb.crda.collection")
    String collectionName;

    @ConfigProperty(name = "quarkus.mongodb.crda.idempotent.collection")
    String idempotentCollectionName;

    @Produces
    MongoDbIdempotentRepository mongoDbIdempotentRepository() {
        return new MongoDbIdempotentRepository(mongoClient, idempotentCollectionName, dbName);
    }
}
