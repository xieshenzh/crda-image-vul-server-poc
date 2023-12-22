package org.crda.image.copy;

import org.apache.camel.builder.RouteBuilder;
import org.crda.exec.ExecErrorProcessor;
import org.crda.image.BasicAuthProcessor;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.exec;
import static org.crda.sbom.Constants.credsHeader;

public class SkopeoRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        onException(RuntimeException.class)
                .handled(true)
                .logHandled(true)
                .logStackTrace(true)
                .stop();

        from(direct("skopeoCopy"))
                .process(new BasicAuthProcessor())
                .choice()
                .when(header(credsHeader).isNotNull())
                .toD(exec("skopeo?args=RAW(copy --src-creds=${header.creds} docker://${header.imageRef} oci:${header.imagePath}:${header.imageRef})"))
                .otherwise()
                .toD(exec("skopeo?args=RAW(copy docker://${header.imageRef} oci:${header.imagePath}:${header.imageRef})"))
                .end()
                .process(new ExecErrorProcessor());
    }
}
