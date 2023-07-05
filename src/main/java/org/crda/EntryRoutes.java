package org.crda;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.crda.fabric8.ImageName;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.camel.support.builder.PredicateBuilder.or;

public class EntryRoutes extends RouteBuilder {

    public EntryRoutes() {
    }

    @Override
    public void configure() throws Exception {
        restConfiguration().contextPath("/image")
                .bindingMode(RestBindingMode.json)
                .clientRequestValidation(true);

//        onException(RegistryUnsupportedException.class)
//                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(422))
//                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
//                .handled(true)
//                .setBody().simple("${exception.message}");
//
//        onException(InvalidImageRefException.class)
//                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
//                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
//                .handled(true)
//                .setBody().simple("${exception.message}");

        rest("/vulnerabilities")
                .get()
                .param().name("image").type(RestParamType.query).required(true).description("Image reference").endParam()
                .param().name("platform").type(RestParamType.query).required(false).description("Image platform").endParam()
                .to("direct:getVulnerabilities");

        from("direct:getVulnerabilities")
                .to("direct:parseImageName")
                .to("direct:execSkopeo")
                .to("direct:checkImageFound")
                .to("direct:queryMongoDB");

        from("direct:execSkopeo")
                .to("direct:skopeoInspectRaw")
                .choice()
                .when(body().isNull())
                .to("direct:skopeoInspect")
                .endChoice();

        from("direct:checkImageFound")
                .choice()
                .when(or(body().isNull(), bodyAs(List.class).method("isEmpty").isEqualTo(true)))
                .process(exchange -> {
                    String image = exchange.getIn().getHeader("image", String.class);
                    String platform = exchange.getIn().getHeader("platform", String.class);
                    Optional.ofNullable(platform).ifPresentOrElse(
                            p -> {
                                throw new IllegalArgumentException(String.format("Image %s with platform %s is not found", image, p));
                            },
                            () -> {
                                throw new IllegalArgumentException(String.format("Image %s is not found", image));
                            });
                });

        from("direct:parseImageName")
                .process(exchange -> {
                    try {
                        String image = exchange.getIn().getHeader("image", String.class);
                        ImageName imageName = new ImageName(image);
                        String imageRegistryRepo = imageName.getNameWithoutTag();
                        exchange.getIn().setHeader("imageRegRepo", imageRegistryRepo);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e.getMessage());
                    }
                });

        from("direct:queryMongoDB")
                .split(body(), new GroupedBodyAggregationStrategy())
                .stopOnException()
                .parallelProcessing()
                .process(exchange -> {
                    String registryRepo = exchange.getIn().getHeader("imageRegRepo", String.class);
                    String digest = exchange.getIn().getBody(String.class);
                    exchange.getIn().setBody(registryRepo + "@" + digest);
                })
                .to("direct:findImageVulnerabilities")
                .end()
                .setBody(exchange -> {
                    List<?> results = exchange.getIn().getBody(List.class);
                    Map<String, org.crda.mongodb.model.Vulnerability> vulnerabilityMap = results.stream()
                            .filter(e -> e instanceof org.crda.mongodb.model.Image)
                            .map(e -> ((org.crda.mongodb.model.Image) e))
                            .map(org.crda.mongodb.model.Image::getVulnerabilities)
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toMap(org.crda.mongodb.model.Vulnerability::getId,
                                    v -> v,
                                    (k1, k2) -> k1));
                    return vulnerabilityMap.values();
                });

    }
}


