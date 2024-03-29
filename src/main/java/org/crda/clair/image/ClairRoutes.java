package org.crda.clair.image;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.crda.exec.ExecErrorProcessor;
import org.crda.clair.image.quay.Secscan;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.exec;

public class ClairRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        onException(RuntimeException.class)
                .handled(true)
                .logHandled(true)
                .logStackTrace(true)
                .stop();

        from(direct("clairReport"))
                .toD(exec("clair-action?args=report --format=quay --db-path={{clair.db.path}} --image-ref ${body}"))
                .process(new ExecErrorProcessor())
                .unmarshal()
                .json(JsonLibrary.Jackson, Secscan.class);
    }
}
