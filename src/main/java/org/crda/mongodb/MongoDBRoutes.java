package org.crda.mongodb;

import com.mongodb.client.model.Filters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.apache.camel.component.mongodb.processor.idempotent.MongoDbIdempotentRepository;
import org.bson.conversions.Bson;

@ApplicationScoped
public class MongoDBRoutes extends RouteBuilder {

    @Inject
    private MongoDbIdempotentRepository idempotentRepository;
    @Override
    public void configure() throws Exception {
        //TODO
        onException(RuntimeException.class);

        from("seda:filterDuplicateImage")
                .idempotentConsumer(body()).idempotentRepository(idempotentRepository);

        from("direct:saveImageVulnerabilities")
                .to("mongodb:mongoClient?database=crda&collection=vuls&collectionIndex={\"id\":1}&operation=save");

        from("direct:findImageVulnerabilities")
                .setHeader(MongoDbConstants.CRITERIA, new Expression() {
                    @Override
                    public <T> T evaluate(Exchange exchange, Class<T> type) {
                        String id = exchange.getIn().getBody(String.class);
                        Bson eq = Filters.eq("id", id);
                        return exchange.getContext().getTypeConverter().convertTo(type, eq);
                    }
                })
                .to("mongodb:mongoClient?database=crda&collection=vuls&operation=findOneByQuery");
    }
}
