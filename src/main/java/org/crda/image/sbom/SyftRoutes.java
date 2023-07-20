package org.crda.image.sbom;

import org.apache.camel.builder.RouteBuilder;
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
                .process(new ExecErrorProcessor());
    }
}
