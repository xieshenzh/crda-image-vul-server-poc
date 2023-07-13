package org.crda;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.apache.camel.spi.IdempotentRepository;
import org.crda.cache.model.Image;
import org.crda.cache.model.Vulnerability;
import org.crda.image.ImageNameProcessor;
import org.crda.image.ImageRefProcessor;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.support.builder.PredicateBuilder.or;
import static org.crda.Constants.*;

@ApplicationScoped
public class EntryRoutes extends RouteBuilder {

    @Inject
    IdempotentRepository idempotentRepository;

    public EntryRoutes() {
    }

    @Override
    public void configure() throws Exception {
        restConfiguration().contextPath("/image")
                .bindingMode(RestBindingMode.json)
                .clientRequestValidation(true);

        onException(IllegalArgumentException.class)
                .logStackTrace(false)
                .setHeader(HTTP_RESPONSE_CODE, constant(400))
                .setHeader(CONTENT_TYPE, constant("text/plain"))
                .handled(true)
                .setBody().simple("${exception.message}");

        rest("/vulnerabilities")
                .get()
                .param().name("image").type(RestParamType.query).required(true).description("Image reference").endParam()
                .param().name("platform").type(RestParamType.query).required(false).description("Image platform").endParam()
                .to("direct:getVulnerabilities");

        from("direct:getVulnerabilities")
                .to("direct:parseImageName")
                .to("direct:execSkopeo")
                .to("direct:checkImageFound")
                .to("direct:queryCache")
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

        from("direct:parseImageName")
                .process(new ImageRefProcessor())
                .process(new ImageNameProcessor());

        from("direct:queryCache")
                .split(body(), new GroupedBodyAggregationStrategy())
                .stopOnException()
                .parallelProcessing()
                .process(new ImageDigestProcessor())
                .to("direct:findImageVulnerabilities")
                .end()
                .choice()
                .when(or(body().isNull(), bodyAs(List.class).method("isEmpty").isEqualTo(true)))
                .process(exchange -> {
                    List<?> digests = exchange.getIn().getHeader(digestsHeader, List.class);
                    exchange.getIn().setHeader(digestsNotFoundHeader, digests);
                })
                .otherwise()
                .setBody(exchange -> {
                    List<?> results = exchange.getIn().getBody(List.class);

                    Set<String> digestSet = results.stream()
                            .filter(e -> e instanceof Image)
                            .map(e -> ((Image) e))
                            .map(Image::getDigest)
                            .collect(Collectors.toSet());

                    List<?> digests = exchange.getIn().getHeader(digestsHeader, List.class);
                    List<?> digestsMissing = digests.stream().filter(d -> !digestSet.contains(d)).toList();
                    exchange.getIn().setHeader(digestsNotFoundHeader, digestsMissing);

                    Map<String, Vulnerability> vulnerabilityMap = results.stream()
                            .filter(e -> e instanceof Image)
                            .map(e -> ((Image) e))
                            .map(Image::getVulnerabilities)
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toMap(Vulnerability::getId,
                                    v -> v,
                                    (k1, k2) -> k1));
                    return vulnerabilityMap.values();
                })
                .endChoice();

        from("direct:checkData")
                .process(exchange -> {
                    List<?> digests = exchange.getIn().getHeader(digestsHeader, List.class);
                    List<?> digestsNotFound = exchange.getIn().getHeader(digestsNotFoundHeader, List.class);
                    if (digestsNotFound == null || digestsNotFound.isEmpty()) {
                        exchange.getIn().setHeader(scannedHeader, "true");
                    } else if (new HashSet<>(digestsNotFound).containsAll(digests)) {
                        exchange.getIn().setHeader(scannedHeader, "false");
                    } else {
                        exchange.getIn().setHeader(scannedHeader, "partial");
                    }
                })
                .choice()
                .when(header(scannedHeader).isEqualTo("false"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(202))
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .setBody(constant("Image scan is in process. Please try again later."))
                .when(header(scannedHeader).isEqualTo("partial"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(202))
                .endChoice();

        from("direct:scanImages")
                .choice()
                .when(header(digestsNotFoundHeader).isNotNull())
                .split(header(digestsNotFoundHeader))
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


