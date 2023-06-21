package org.crda;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.crda.image.ImageRefProcessor;

import static org.crda.image.Constants.quayRegistry;
import static org.crda.image.Constants.registryHeader;

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
                .process(new ImageRefProcessor())
                .choice()
                .when(header(registryHeader).isEqualTo(quayRegistry))
                .to("direct:manifest").to("direct:quay-vulnerabilities")
                .otherwise()
                .to("direct:hello")
                .end();

        from("direct:hello")
                .toD("https://httpbin.org/get?bridgeEndpoint=true&test=${header.test}")
                .unmarshal()
                .json(JsonLibrary.Jackson);
    }
}


