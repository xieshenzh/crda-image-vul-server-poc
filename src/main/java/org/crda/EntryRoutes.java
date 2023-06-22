package org.crda;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.crda.image.ImageRefProcessor;
import org.crda.registry.RegistryUnsupportedException;

import static org.crda.registry.Constants.quayRegistry;
import static org.crda.image.Constants.registryHeader;

public class EntryRoutes extends RouteBuilder {

    public EntryRoutes() {
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
                .to("direct:manifest")
                .to("direct:quay-vulnerabilities")
                .otherwise()
                .process(exchange -> {
                    String image = exchange.getIn().getHeader("image", String.class);
                    String registry = exchange.getIn().getHeader(registryHeader, String.class);
                    throw new RegistryUnsupportedException(String.format("Registry %s of image %s is not supported", registry, image));
                })
                .end();

        from("direct:hello")
                .toD("https://httpbin.org/get?bridgeEndpoint=true&test=${header.test}")
                .unmarshal()
                .json(JsonLibrary.Jackson);
    }
}


