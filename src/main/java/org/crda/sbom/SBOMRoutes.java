package org.crda.sbom;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.GroupedMessageAggregationStrategy;
import org.crda.image.ImageDigestProcessor;

import java.util.ArrayList;
import java.util.List;

import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.support.builder.PredicateBuilder.and;
import static org.crda.image.Constants.imageRefHeader;
import static org.crda.sbom.Constants.imagesExecHeader;

@ApplicationScoped
public class SBOMRoutes extends RouteBuilder {

    public SBOMRoutes() {
    }

    @Inject
    SBOMIdempotentRepository idempotentRepository;

    @Override
    public void configure() throws Exception {
        onException(RuntimeException.class)
                .handled(true)
                .setHeader(HTTP_RESPONSE_CODE, constant(500))
                .setHeader(CONTENT_TYPE, constant("text/plain"))
                .setBody().simple("${exception.message}");

        from("direct:getSBOMs")
                .to("direct:parseImageName")
                .to("direct:getImageManifests")
                .to("direct:checkImageFound")
                .to("direct:queryImageSBOMs")
                .to("direct:checkSBOMData")
                .to("direct:generateImagesSBOM");

        from("direct:queryImageSBOMs")
                .split(body(), new GroupedMessageAggregationStrategy())
                .stopOnException()
                .parallelProcessing()
                .process(new ImageDigestProcessor())
                .to("direct:findImageSBOM")
                .end()
                .process(exchange -> {
                    List<?> messages = exchange.getIn().getBody(List.class);
                    List<String> missing = new ArrayList<>();
                    List<Object> sboms = messages.stream()
                            .map(o -> (Message) o)
                            .filter(m -> {
                                Object body = m.getBody();
                                if (body == null) {
                                    missing.add(m.getHeader(imageRefHeader, String.class));
                                    return false;
                                }
                                return true;
                            })
                            .map(Message::getBody)
                            .toList();
                    if (!missing.isEmpty()) {
                        exchange.getIn().setHeader(imagesExecHeader, missing);
                    }
                    if (!sboms.isEmpty()) {
                        exchange.getIn().setBody(sboms);
                    } else {
                        exchange.getIn().setBody(null);
                    }
                });

        from("direct:checkSBOMData")
                .choice()
                .when(body().isNull())
                .process(exchange -> {
                    int i = 0;
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(202))
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .setBody(constant("Image scan is in process. Please try again later."))
                .when(and(body().isNotNull(), header(imagesExecHeader).isNotNull()))
                .process(exchange -> {
                    int i = 0;
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(202))
                .otherwise()
                .process(exchange -> {
                    int i = 0;
                })
                .endChoice();

        from("direct:generateImagesSBOM")
                .choice()
                .when(header(imagesExecHeader).isNotNull())
                .split(header(imagesExecHeader))
                .parallelProcessing()
                .setHeader(imageRefHeader, body())
                .process(exchange -> {
                    int i = 0;
                })
                .to("seda:generateSBOM?waitForTaskToComplete=Never&timeout=0")
                .end()
                .endChoice();

        //TODO
        from("seda:generateSBOM?waitForTaskToComplete=Never&timeout=0")
                .process(exchange -> {
                    int i = 0;
                })
                .idempotentConsumer(header(imageRefHeader)).idempotentRepository(idempotentRepository)
                .process(exchange -> {
                    int i = 0;
                })
                .process(new CreateTempDirectoryProcessor())
                .process(exchange -> {
                    int i = 0;
                })
                .toD("file:${header.imagePath}")
                .process(exchange -> {
                    int i = 0;
                })
                .to("direct:skopeoCopy")
                .to("direct:syft")
                .to("direct:saveImageSBOM")
                .process(exchange -> {
                    int i = 0;
                })
                .process(new DeleteTempDirectoryProcessor())
                .process(exchange -> {
                    String key = exchange.getIn().getHeader(imageRefHeader, String.class);
                    idempotentRepository.remove(key);
                });
    }
}
