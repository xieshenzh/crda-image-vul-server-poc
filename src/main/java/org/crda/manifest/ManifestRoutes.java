package org.crda.manifest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.camel.support.builder.PredicateBuilder.and;
import static org.apache.camel.support.builder.PredicateBuilder.or;
import static org.crda.manifest.Constants.digestHeader;

public class ManifestRoutes extends RouteBuilder {

    public ManifestRoutes() {
    }

    @Override
    public void configure() throws Exception {
        from("direct:manifest")
                .to("direct:manifest-body")
                .choice()
                .when(body().isNull())
                .to("direct:manifest-headers")
                .end();

        from("direct:manifest-body")
                .choice()
                .when(or(header("platform").isNull(), header("platform").isEqualTo("")))
                .to("direct:manifest-body-all")
                .otherwise()
                .setBody(constant((Object) null))
                .end();

        from("direct:manifest-body-all")
                .toD("exec:regctl?args=manifest get --format raw-body ${header.image}")
                .to("direct:manifest-body-digests");

        from("direct:manifest-body-platform")
                .toD("exec:regctl?args=manifest get -p ${header.platform} --format raw-body ${header.image}")
                .to("direct:manifest-body-digests");

        from("direct:manifest-body-digests")
                .process(new ManifestClientErrorProcessor())
                .unmarshal()
                .json(JsonLibrary.Jackson, Manifest.class)
                .process(exchange -> {
                    Manifest manifest = exchange.getIn().getBody(Manifest.class);
                    Optional.ofNullable(manifest.getManifests())
                            .ifPresentOrElse(m -> exchange.getIn().setBody(m.stream().map(Descriptor::getDigest).collect(Collectors.toList())),
                                    () -> exchange.getIn().setBody(null));
                });

        from("direct:manifest-headers")
                .choice()
                .when(and(header("platform").isNotNull(), header("platform").isNotEqualTo("")))
                .to("direct:manifest-headers-platform")
                .otherwise()
                .to("direct:manifest-headers-all")
                .end();

        from("direct:manifest-headers-all")
                .toD("exec:regctl?args=manifest get --format raw-headers ${header.image}")
                .to("direct:manifest-headers-digest");

        from("direct:manifest-headers-platform")
                .toD("exec:regctl?args=manifest get -p ${header.platform} --format raw-headers ${header.image}")
                .to("direct:manifest-headers-digest");

        from("direct:manifest-headers-digest")
                .process(new ManifestClientErrorProcessor())
                .process(exchange -> {
                    String headers = exchange.getIn().getBody(String.class);
                    Properties properties = new Properties();
                    properties.load(new StringReader(headers));
                    Optional.ofNullable(properties.get(digestHeader)).ifPresent(digest ->
                            exchange.getIn().setBody(Stream.of(digest).collect(Collectors.toList())));
                });
    }
}
