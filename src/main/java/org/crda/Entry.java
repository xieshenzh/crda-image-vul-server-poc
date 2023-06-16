package org.crda;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.crda.image.model.Image;
import org.crda.image.ImageRefProcessor;

import java.util.Collections;

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
                .param().name("arch").type(RestParamType.query).required(false).description("Image platform architecture").endParam()
                .param().name("os").type(RestParamType.query).required(false).description("Image platform OS").endParam()
                .to("direct:getVulnerabilities");

        from("direct:getVulnerabilities").process(new ImageRefProcessor())
                .setBody(exchange -> {
                    String image = exchange.getIn().getHeader("imageStr", String.class);
                    return new Image(image, Collections.emptyList());
                });
    }
}


