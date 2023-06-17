package org.crda;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.crda.image.ImageRefProcessor;
import org.crda.image.model.Image;

import java.util.Collections;

import static org.apache.camel.support.builder.PredicateBuilder.or;
import static org.crda.image.ImageRefProcessor.*;

public class Entry extends RouteBuilder {

    public Entry() {
    }

    @Override
    public void configure() throws Exception {
        restConfiguration().contextPath("/image")
                .bindingMode(RestBindingMode.json)
                .clientRequestValidation(true);

        rest("/vulnerabilities")
                .get()
                .param().name("image").type(RestParamType.query).required(true).description("Image reference").endParam()
                .param().name("platform").type(RestParamType.query).required(false).description("Image platform").endParam()
                .to("direct:getVulnerabilities");

        from("direct:getVulnerabilities")
                .toD("exec:regctl?args=manifest get ${header.image}")
                .setBody(exchange -> {
                    String image = exchange.getIn().getBody(String.class);
                    return new Image(image, Collections.emptyList());
                })
                .end();

        from("direct:hello")
                .toD("https://httpbin.org/get?bridgeEndpoint=true&test=${header.test}")
                .unmarshal()
                .json(JsonLibrary.Jackson);
    }
}


