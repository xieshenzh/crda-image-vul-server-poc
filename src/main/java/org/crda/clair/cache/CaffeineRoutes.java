package org.crda.clair.cache;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.caffeine.CaffeineConstants;
import org.crda.clair.cache.model.Image;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.caffeineCache;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.crda.image.Constants.imageRefHeader;

public class CaffeineRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        from(direct("saveClairImageVulnerabilities"))
                .process(exchange -> {
                    Image image = exchange.getIn().getBody(Image.class);
                    String ref = exchange.getIn().getHeader(imageRefHeader, String.class);
                    image.setId(ref);
                    exchange.getIn().setHeader(CaffeineConstants.VALUE, image);
                })
                .setHeader(CaffeineConstants.KEY, header(imageRefHeader))
                .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_PUT))
                .to(caffeineCache("clairVuls?evictionType=time_based"))
                .choice()
                .when(header(CaffeineConstants.ACTION_SUCCEEDED).isEqualTo(false))
                .throwException(RuntimeException.class, "Failed to write to cache")
                .endChoice();

        from(direct("findClairImageVulnerabilities"))
                .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_GET))
                .setHeader(CaffeineConstants.KEY, body())
                .to(caffeineCache("clairVuls?evictionType=time_based"))
                .choice()
                .when(header(CaffeineConstants.ACTION_SUCCEEDED).isEqualTo(false))
                .throwException(RuntimeException.class, "Failed to read from cache")
                .when(header(CaffeineConstants.ACTION_HAS_RESULT).isEqualTo(false))
                .setBody(constant((Object) null))
                .endChoice();
    }
}
