package org.crda.image.sbom;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.crda.exec.ExecErrorProcessor;

public class SyftRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        onException(RuntimeException.class)
                .handled(true)
                .logHandled(true)
                .logStackTrace(true)
                .stop();

        from("direct:syft")
                .toD("exec:syft?args=oci-dir:${header.imagePath} --scope all-layers -o cyclonedx-json")
                .process(new ExecErrorProcessor())
                .process(exchange -> {
                    int i = 0;
                })
                .unmarshal()
                .json(JsonLibrary.Jackson, JsonNode.class);
    }
}
