package org.crda.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.crda.model.Image;

import java.util.Collections;

public class Routes extends RouteBuilder {

    public Routes() {
    }

    @Override
    public void configure() throws Exception {
        restConfiguration().contextPath("/image/{image}").bindingMode(RestBindingMode.json);

        rest("/vulnerabilities")
                .get()
                .to("direct:getVulnerabilities");

        from("direct:getVulnerabilities")
                .setBody().constant(new Image("test", Collections.emptyList()));
    }
}
