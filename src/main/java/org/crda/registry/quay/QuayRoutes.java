package org.crda.registry.quay;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;

public class QuayRoutes extends RouteBuilder {
    public QuayRoutes() {
    }

    @Override
    public void configure() throws Exception {
        from("direct:quay-vulnerabilities")
                .split(body(), new GroupedBodyAggregationStrategy())
                .parallelProcessing()
                .removeHeaders("CamelHttp*")
                .process(exchange -> {
                    exchange.getProperties();
                })
//                .toD("https://httpbin.org/get?bridgeEndpoint=true&test=${header.test}")
                .removeHeader(Exchange.HTTP_URI)
                .setHeader(Exchange.HTTP_PATH, simple("/api/v1/repository/${header.repository}/manifest/${body}/security"))
                .setHeader(Exchange.HTTP_QUERY, constant("vulnerabilities=true"))
                .setHeader(Exchange.HTTP_HOST, constant("quay.io"))
                .toD("https://quay.io")
                .end();
        ;
    }
}
