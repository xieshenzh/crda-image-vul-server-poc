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

public class SkopeoRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        from("direct:skopeoInspect")
                .toD("exec:skopeo?args=inspect docker://${header.image}")
                .process(new ExecErrorProcessor())
                .unmarshal()
                .json(JsonLibrary.Jackson, Manifest.class)
                .process(exchange -> {
                    Manifest manifest = exchange.getIn().getBody(Manifest.class);
                    Optional.ofNullable(manifest.getDigest())
                            .ifPresentOrElse(
                                    d -> exchange.getIn().setBody(Collections.singletonList(d)),
                                    () -> exchange.getIn().setBody(null));
                });

        from("direct:skopeoInspectRaw")
                .toD("exec:skopeo?args=inspect --raw docker://${header.image}")
                .process(new ExecErrorProcessor())
                .unmarshal()
                .json(JsonLibrary.Jackson, org.crda.image.manifest.model.raw.Manifest.class)
                .process(exchange -> {
                    org.crda.image.manifest.model.raw.Manifest manifest = exchange.getIn().getBody(org.crda.image.manifest.model.raw.Manifest.class);
                    String platformStr = exchange.getIn().getHeader("platform", String.class);
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
                                                m.stream().map(Descriptor::getDigest).collect(Collectors.toList())),
                                        () -> exchange.getIn().setBody(null));
                    }
                });
    }
}
