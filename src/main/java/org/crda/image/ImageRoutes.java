package org.crda.image;

import org.apache.camel.builder.RouteBuilder;

import java.util.List;
import java.util.Optional;

import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.support.builder.PredicateBuilder.or;
import static org.crda.image.Constants.*;

public class ImageRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        onException(IllegalArgumentException.class)
                .logStackTrace(false)
                .setHeader(HTTP_RESPONSE_CODE, constant(400))
                .setHeader(CONTENT_TYPE, constant("text/plain"))
                .handled(true)
                .setBody().simple("${exception.message}");

        from("direct:parseImageName")
                .process(new ImageRefProcessor())
                .process(new ImageNameProcessor());

        from("direct:getImageManifests")
                .to("direct:skopeoInspectRaw")
                .choice()
                .when(body().isNull())
                .to("direct:skopeoInspect")
                .endChoice();

        from("direct:checkImageFound")
                .choice()
                .when(or(body().isNull(), bodyAs(List.class).method("isEmpty").isEqualTo(true)))
                .process(exchange -> {
                    String image = exchange.getIn().getHeader(imageHeader, String.class);
                    String platform = exchange.getIn().getHeader(platformHeader, String.class);
                    Optional.ofNullable(platform).ifPresentOrElse(
                            p -> {
                                throw new IllegalArgumentException(String.format("Image %s with platform %s is not found", image, p));
                            },
                            () -> {
                                throw new IllegalArgumentException(String.format("Image %s is not found", image));
                            });
                })
                .otherwise()
                .process(exchange -> {
                    exchange.getIn().setHeader(digestsHeader, exchange.getIn().getBody());
                })
                .endChoice();
    }
}
