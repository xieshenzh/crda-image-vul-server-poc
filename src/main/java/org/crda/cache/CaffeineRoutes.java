package org.crda.cache;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.caffeine.CaffeineConstants;
import org.crda.cache.model.Image;

import static org.crda.image.Constants.imageRegRepoHeader;

public class CaffeineRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        onException(RuntimeException.class)
                .handled(true)
                .logHandled(true)
                .logStackTrace(true)
                .stop();

        from("direct:saveImageVulnerabilities")
                .process(exchange -> {
                    Image image = exchange.getIn().getBody(Image.class);
                    String regRepo = exchange.getIn().getHeader(imageRegRepoHeader, String.class);
                    String ref = regRepo + "@" + image.getDigest();
                    image.setId(ref);
                    exchange.getIn().setHeader(CaffeineConstants.VALUE, image);
                    exchange.getIn().setHeader(CaffeineConstants.KEY, ref);
                })
                .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_PUT))
                .to("caffeine-cache://vuls?evictionType=time_based")
                .choice()
                .when(header(CaffeineConstants.ACTION_SUCCEEDED).isEqualTo(false))
                .throwException(RuntimeException.class, "Failed to write to cache")
                .endChoice();

        from("direct:findImageVulnerabilities")
                .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_GET))
                .setHeader(CaffeineConstants.KEY, body())
                .to("caffeine-cache://vuls?evictionType=time_based")
                .choice()
                .when(header(CaffeineConstants.ACTION_SUCCEEDED).isEqualTo(false))
                .throwException(RuntimeException.class, "Failed to read from cache")
                .when(header(CaffeineConstants.ACTION_HAS_RESULT).isEqualTo(false))
                .setBody(constant((Object) null))
                .endChoice();
    }
}
