package org.crda.mongodb;

import com.mongodb.client.model.Filters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.crda.mongodb.model.Image;

@ApplicationScoped
public class MongoDBRoutes extends RouteBuilder {

    @Inject
    MongoDBConfiguration configuration;

    @Override
    public void configure() throws Exception {
        //TODO
//        onException(RuntimeException.class);

        from("direct:saveImageVulnerabilities")
                .process(exchange -> {
                    Image image = exchange.getIn().getBody(Image.class);
                    String regRepo = exchange.getIn().getHeader("imageRegRepo", String.class);
                    image.setId(regRepo + "@" + image.getDigest());
                    exchange.getIn().setBody(image);
                })
                .to("mongodb:crda?database=" + configuration.dbName + "&collection=" +
                        configuration.collectionName + "&collectionIndex={\"id\":1}&operation=save");

        from("direct:findImageVulnerabilities")
                .setHeader(MongoDbConstants.CRITERIA, new Expression() {
                    @Override
                    public <T> T evaluate(Exchange exchange, Class<T> type) {
                        String id = exchange.getIn().getBody(String.class);
                        Bson eq = Filters.eq("id", id);
                        return exchange.getContext().getTypeConverter().convertTo(type, eq);
                    }
                })
                .to("mongodb:crda?database=" + configuration.dbName + "&collection=" +
                        configuration.collectionName + "&operation=findOneByQuery")
                .filter(body().isNotNull())
                .process(exchange -> {
                    Document result = exchange.getIn().getBody(Document.class);
                    exchange.getIn().setBody(result.toJson());
                })
                .unmarshal()
                .json(JsonLibrary.Jackson, Image.class);
    }
}
