package org.crda.image.manifest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.crda.exec.ExecErrorProcessor;
import org.crda.image.manifest.model.output.Manifest;
import org.crda.image.manifest.model.raw.Descriptor;
import org.crda.image.manifest.model.raw.Platform;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.crda.image.Constants.platformHeader;
import static org.crda.image.Constants.supportedPlatforms;
import static org.crda.sbom.Constants.credsHeader;

public class SkopeoRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        onException(RuntimeException.class)
                .handled(true)
                .setHeader(HTTP_RESPONSE_CODE, constant(500))
                .setHeader(CONTENT_TYPE, constant("text/plain"))
                .setBody().simple("${exception.message}");

        from("direct:skopeoInspect")
                .choice()
                .when(header(credsHeader).isNotNull())
                .toD("exec:skopeo?args=inspect --creds=${header.creds} docker://${header.image}")
                .otherwise()
                .toD("exec:skopeo?args=inspect docker://${header.image}")
                .end()
                .process(new ExecErrorProcessor())
                .unmarshal()
                .json(JsonLibrary.Jackson, Manifest.class)
                .process(exchange -> {
                    Manifest manifest = exchange.getIn().getBody(Manifest.class);
                    String platformStr = exchange.getIn().getHeader(platformHeader, String.class);
                    if (platformStr != null) {
                        Platform platform = new Platform(platformStr);
                        Platform imagePlatform = new Platform().os(manifest.getOs()).arch(manifest.getArchitecture());
                        if (!platform.equals(imagePlatform)) {
                            exchange.getIn().setBody(null);
                            return;
                        }
                    }

                    Optional.ofNullable(manifest.getDigest())
                            .ifPresentOrElse(
                                    d -> exchange.getIn().setBody(Collections.singletonList(d)),
                                    () -> exchange.getIn().setBody(null));
                });

        from("direct:skopeoInspectRaw")
                .choice()
                .when(header(credsHeader).isNotNull())
                .toD("exec:skopeo?args=inspect --raw --creds=${header.creds} docker://${header.image}")
                .otherwise()
                .toD("exec:skopeo?args=inspect --raw docker://${header.image}")
                .end()
                .process(new ExecErrorProcessor())
                .unmarshal()
                .json(JsonLibrary.Jackson, org.crda.image.manifest.model.raw.Manifest.class)
                .process(exchange -> {
                    org.crda.image.manifest.model.raw.Manifest manifest = exchange.getIn().getBody(org.crda.image.manifest.model.raw.Manifest.class);
                    String platformStr = exchange.getIn().getHeader(platformHeader, String.class);
                    if (platformStr != null) {
                        Platform platform = new Platform(platformStr);
                        Optional.ofNullable(manifest.getManifests())
                                .ifPresentOrElse(
                                        m -> exchange.getIn().setBody(
                                                m.stream().filter(p -> platform.equals(p.getPlatform()))
                                                        .map(Descriptor::getDigest).collect(Collectors.toList())),
                                        () -> exchange.getIn().setBody(null));
                    } else {
                        Optional.ofNullable(manifest.getManifests())
                                .ifPresentOrElse(
                                        m -> exchange.getIn().setBody(
                                                m.stream()
                                                        .filter(p -> supportedPlatforms.contains(p.getPlatform()))
                                                        .map(Descriptor::getDigest).collect(Collectors.toList())),
                                        () -> exchange.getIn().setBody(null));
                    }
                });
    }
}
