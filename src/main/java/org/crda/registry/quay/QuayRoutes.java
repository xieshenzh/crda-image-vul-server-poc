package org.crda.registry.quay;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.crda.clair.model.quay.Feature;
import org.crda.clair.model.quay.Secscan;
import org.crda.clair.model.quay.Vulnerability;
import org.crda.image.Image;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.camel.Exchange.*;

public class QuayRoutes extends RouteBuilder {
    public QuayRoutes() {
    }

    @Override
    public void configure() throws Exception {
        onException(QuayRequestException.class)
                .setHeader(HTTP_RESPONSE_CODE, simple("${exception.getCode()}"))
                .setHeader(CONTENT_TYPE, constant("text/plain"))
                .handled(true)
                .setBody().simple("${exception.message}");

        onException(HttpOperationFailedException.class)
                .logStackTrace(false)
                .setHeader(HTTP_RESPONSE_CODE, simple("${exception.getStatusCode()}"))
                .setHeader(CONTENT_TYPE, constant("text/plain"))
                .handled(true)
                .setBody().simple("Message: ${exception.message}, Response: ${exception.getResponseBody()}");

        from("direct:quay-vulnerabilities")
                .split(body(), new GroupedBodyAggregationStrategy())
                .stopOnException()
                .parallelProcessing()
                .removeHeaders("CamelHttp*")
                .removeHeader("User-Agent")
                .setHeader(HTTP_PATH, simple("/api/v1/repository/${header.repository}/manifest/${body}/security"))
                .setHeader(HTTP_QUERY, constant("vulnerabilities=true"))
                .toD("https://quay.io")
                .process(exchange -> {
                    Integer code = exchange.getIn().getHeader(HTTP_RESPONSE_CODE, Integer.class);
                    String message = exchange.getIn().getBody(String.class);
                    if (code != 200) {
                        throw new QuayRequestException(message, code);
                    }
                })
                .unmarshal()
                .json(JsonLibrary.Jackson, Secscan.class)
                .end()
                .setBody(exchange -> {
                    String image = exchange.getIn().getHeader("image", String.class);
                    List<?> results = exchange.getIn().getBody(List.class);
                    Map<String, org.crda.image.Vulnerability> vulnerabilityMap = results.stream()
                            .filter(e -> e instanceof Secscan)
                            .map(e -> ((Secscan) e))
                            .filter(e -> "scanned".equals(e.getStatus())
                                    && e.getData() != null
                                    && e.getData().getLayer() != null
                                    && e.getData().getLayer().getFeatures() != null)
                            .map(e -> e.getData().getLayer().getFeatures())
                            .flatMap(Collection::stream)
                            .map(Feature::getVulnerabilities)
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toMap(Vulnerability::getName,
                                    v -> new org.crda.image.Vulnerability(v.getName(), v.getSeverity()),
                                    (k1, k2) -> k1));

                    return new Image(image, new ArrayList<>(vulnerabilityMap.values()));
                });
    }
}
