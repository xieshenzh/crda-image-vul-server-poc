package org.crda.image.copy;

import org.apache.camel.builder.RouteBuilder;
import org.crda.exec.ExecErrorProcessor;

public class SkopeoRoutes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        onException(RuntimeException.class)
                .handled(true)
                .logHandled(true)
                .logStackTrace(true)
                .stop();

        //TODO
        from("direct:skopeoCopy")
                .toD("exec:skopeo?args=copy docker://${header.imageRef} oci:${header.imagePath}:${header.imageRef}")
                .process(new ExecErrorProcessor())
                .process(exchange -> {
                    int i = 0;
                });
    }
}
