package org.crda.cache;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.caffeine.CaffeineConstants;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.caffeineCache;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.crda.image.Constants.imageRefHeader;

public class CaffeineRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        from(direct("saveImageSBOM"))
                .setHeader(CaffeineConstants.KEY, header(imageRefHeader))
                .setHeader(CaffeineConstants.VALUE, body())
                .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_PUT))
                .to(caffeineCache("sboms?evictionType=size_based"))
                .choice()
                .when(header(CaffeineConstants.ACTION_SUCCEEDED).isEqualTo(false))
                .throwException(RuntimeException.class, "Failed to write to cache")
                .endChoice();

        from(direct("findImageSBOM"))
                .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_GET))
                .setHeader(CaffeineConstants.KEY, header(imageRefHeader))
                .to(caffeineCache("sboms?evictionType=size_based"))
                .choice()
                .when(header(CaffeineConstants.ACTION_SUCCEEDED).isEqualTo(false))
                .throwException(RuntimeException.class, "Failed to read from cache")
                .when(header(CaffeineConstants.ACTION_HAS_RESULT).isEqualTo(false))
                .setBody(constant((Object) null))
                .endChoice();
    }
}
