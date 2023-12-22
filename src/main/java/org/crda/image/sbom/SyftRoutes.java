package org.crda.image.sbom;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.crda.exec.ExecErrorProcessor;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.exec;

public class SyftRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        onException(RuntimeException.class)
                .handled(true)
                .logHandled(true)
                .logStackTrace(true)
                .stop();

        from(direct("syft"))
                .toD(exec("syft?args=RAW(oci-dir:${header.imagePath} --scope all-layers -o cyclonedx-json)"))
                .process(new ExecErrorProcessor())
                .unmarshal()
                .json(JsonLibrary.Jackson, JsonNode.class)
                .to(direct("setPurl"));

        // Not support passing credentials per request
        from(direct("syft-registry"))
                .toD(exec("syft?args=RAW(registry:${header.imageRef} --scope all-layers -o cyclonedx-json)"))
                .process(new ExecErrorProcessor())
                .unmarshal()
                .json(JsonLibrary.Jackson, JsonNode.class)
                .to(direct("setPurl"));
    }
}
