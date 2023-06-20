package org.crda.manifest;

import org.apache.camel.builder.RouteBuilder;
import org.crda.image.Image;

import java.util.Collections;

import static org.apache.camel.support.builder.PredicateBuilder.and;

public class ManifestRoutes extends RouteBuilder {

    public ManifestRoutes() {
    }

    @Override
    public void configure() throws Exception {
        from("direct:manifest")
                .choice()
                .when(and(header("platform").isNotNull(), header("platform").isNotEqualTo("")))
                .to("direct:manifest-platform")
                .otherwise()
                .to("direct:manifest-all")
                .end();

        from("direct:manifest-all")
                .toD("exec:regctl?args=manifest get ${header.image}")
                .to("direct:manifest-result");

        from("direct:manifest-platform")
                .toD("exec:regctl?args=manifest get -p ${header.platform} ${header.image}")
                .to("direct:manifest-result");

        from("direct:manifest-result")
                .process(exchange -> {
                    int exitCode = exchange.getIn().getHeader("CamelExecExitValue", Integer.class);
                    if (exitCode != 0) {
                        String errMessage = exchange.getIn().getHeader("CamelExecStdErr", String.class);
                        throw new ManifestClientException(String.format("regctl error, code: %d, message: %s", exitCode, errMessage));
                    }
                })
                .setBody(exchange -> {
                    String image = exchange.getIn().getBody(String.class);
                    return new Image(image, Collections.emptyList());
                });
    }
}
