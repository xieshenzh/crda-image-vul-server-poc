package org.crda.clair;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.crda.clair.model.quay.Secscan;
import org.crda.exec.ExecErrorProcessor;

public class ClairRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:clair-action-report")
                .toD("exec:clair-action?args=report --format=quay --db-path=matcher.db --image-ref quay.io/keycloak/keycloak")
                .process(new ExecErrorProcessor())
                .unmarshal()
                .json(JsonLibrary.Jackson, Secscan.class);
    }
}
