package org.crda;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mongodb.processor.idempotent.MongoDbIdempotentRepository;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.crda.image.ImageNameProcessor;
import org.crda.mongodb.model.Image;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.camel.support.builder.PredicateBuilder.or;

@ApplicationScoped
public class EntryRoutes extends RouteBuilder {

    @Inject
    MongoDbIdempotentRepository idempotentRepository;

    public EntryRoutes() {
    }

    @Override
    public void configure() throws Exception {
        restConfiguration().contextPath("/image")
                .bindingMode(RestBindingMode.json)
                .clientRequestValidation(true);

        onException(IllegalArgumentException.class)
                .process(exchange -> {
                    int i = 0;
                });

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
                .to("direct:queryMongoDB")
                .to("direct:checkData")
                .to("direct:scanImages");

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
                })
                .otherwise()
                .process(exchange -> {
                    exchange.getIn().setHeader("digests", exchange.getIn().getBody());
                })
                .endChoice();

        from("direct:parseImageName")
                .process(new ImageNameProcessor());

        from("direct:queryMongoDB")
                .split(body(), new GroupedBodyAggregationStrategy())
                .stopOnException()
                .parallelProcessing()
                .process(new ImageDigestProcessor())
                .to("direct:findImageVulnerabilities")
                .end()
                .choice()
                .when(or(body().isNull(), bodyAs(List.class).method("isEmpty").isEqualTo(true)))
                .process(exchange -> {
                    List<?> digests = exchange.getIn().getHeader("digests", List.class);
                    exchange.getIn().setHeader("digestsNotFound", digests);
                })
                .otherwise()
                .setBody(exchange -> {
                    List<?> results = exchange.getIn().getBody(List.class);

                    Set<String> digestSet = results.stream()
                            .filter(e -> e instanceof org.crda.mongodb.model.Image)
                            .map(e -> ((org.crda.mongodb.model.Image) e))
                            .map(org.crda.mongodb.model.Image::getDigest)
                            .collect(Collectors.toSet());

                    List<?> digests = exchange.getIn().getHeader("digests", List.class);
                    List<?> digestsMissing = digests.stream().filter(d -> !digestSet.contains(d)).toList();
                    exchange.getIn().setHeader("digestsNotFound", digestsMissing);

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
                })
                .endChoice();

        from("direct:checkData")
                .process(exchange -> {
                    List<?> digests = exchange.getIn().getHeader("digests", List.class);
                    List<?> digestsNotFound = exchange.getIn().getHeader("digestsNotFound", List.class);
                    if (digestsNotFound == null || digestsNotFound.isEmpty()) {
                        exchange.getIn().setHeader("scanned", "true");
                    } else if (new HashSet<>(digestsNotFound).containsAll(digests)) {
                        exchange.getIn().setHeader("scanned", "false");
                    } else {
                        exchange.getIn().setHeader("scanned", "partial");
                    }
                })
                .choice()
                .when(header("scanned").isEqualTo("false"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(202))
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .setBody(constant("Image scan is in process. Please try again later."))
                .when(header("scanned").isEqualTo("partial"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(202))
                .endChoice();

        from("direct:scanImages")
                .choice()
                .when(header("digestsNotFound").isNotNull())
                .split(header("digestsNotFound"))
                .parallelProcessing()
                .process(new ImageDigestProcessor())
                .to("seda:scanImage?concurrentConsumers=1&waitForTaskToComplete=Never&timeout=0")
                .end()
                .endChoice();

        from("seda:scanImage?concurrentConsumers=1&waitForTaskToComplete=Never&timeout=0")
                .idempotentConsumer(body()).idempotentRepository(idempotentRepository)
                .to("direct:clairReport")
                .convertBodyTo(Image.class)
                .to("direct:saveImageVulnerabilities");
    }
}


