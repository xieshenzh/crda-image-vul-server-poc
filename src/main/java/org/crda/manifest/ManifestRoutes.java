package org.crda.manifest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

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
                .end()
                .choice()
                .when(body().isNull())
                .to("direct:manifest-header")
                .end();

        from("direct:manifest-all")
                .toD("exec:regctl?args=manifest get --format raw-body ${header.image}")
                .to("direct:manifest-digests");

        from("direct:manifest-platform")
                .toD("exec:regctl?args=manifest get -p ${header.platform} --format raw-body ${header.image}")
                .to("direct:manifest-digests-body");

        from("direct:manifest-digests-body")
                .process(new ManifestClientErrorProcessor())
                .unmarshal()
                .json(JsonLibrary.Jackson, Manifest.class)
                .process(exchange -> {
                    Manifest manifest = exchange.getIn().getBody(Manifest.class);
                    Optional.ofNullable(manifest.getManifests()).map(Arrays::<Descriptor>asList)
                            .ifPresentOrElse(d -> exchange.getIn().setBody(d.stream().map(Descriptor::getDigest).collect(Collectors.toList())),
                                    () -> exchange.getIn().setBody(null));
                });
    }
}
