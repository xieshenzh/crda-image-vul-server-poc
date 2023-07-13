package org.crda.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.camel.BindToRegistry;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.caffeine.CaffeineConstants;
import org.crda.cache.model.Image;

public class CaffeineRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        //TODO
//        onException(RuntimeException.class);

        from("direct:saveImageVulnerabilities")
                .process(exchange -> {
                    Image image = exchange.getIn().getBody(Image.class);
                    String regRepo = exchange.getIn().getHeader("imageRegRepo", String.class);
                    String ref = regRepo + "@" + image.getDigest();
                    image.setId(ref);
                    exchange.getIn().setBody(image);
                    exchange.getIn().setHeader("imageRef", ref);
                })
                .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_PUT))
                .setHeader(CaffeineConstants.KEY, header("imageRef"))
                .setHeader(CaffeineConstants.VALUE, body())
                .to("caffeine-cache://crda")
                .process(exchange ->{
                    Object result = exchange.getIn().getHeader(CaffeineConstants.ACTION_HAS_RESULT);
                    Object succeeded = exchange.getIn().getHeader(CaffeineConstants.ACTION_SUCCEEDED);
                });

        from("direct:findImageVulnerabilities")
                .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_GET))
                .setHeader(CaffeineConstants.KEY, body())
                .to("caffeine-cache://crda")
                .process(exchange -> {
                    Object result = exchange.getIn().getHeader(CaffeineConstants.ACTION_HAS_RESULT);
                    Object succeeded = exchange.getIn().getHeader(CaffeineConstants.ACTION_SUCCEEDED);
                });
    }
}
