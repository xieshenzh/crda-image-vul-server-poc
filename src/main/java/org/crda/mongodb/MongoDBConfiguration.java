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

    @ConfigProperty(name = "quarkus.mongodb.database")
    String dbName;

    @ConfigProperty(name = "quarkus.mongodb.collection")
    String collectionName;

    @Produces
    MongoDbIdempotentRepository mongoDbIdempotentRepository() {
        return new MongoDbIdempotentRepository(mongoClient, collectionName, dbName);
    }
}
