package org.crda.exhort;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ExhortRoutes extends RouteBuilder {

    @ConfigProperty(name = "exhort.backend.url", defaultValue = "https://rhda.rhcloud.com")
    String exhortBackendUrl;

    @ConfigProperty(name = "rhda.source", defaultValue = "rhda-image-scan")
    String rhdaSource;

    @ConfigProperty(name = "rhda.token")
    String rhdaToken;

    @ConfigProperty(name = "exhort.snyk.token")
    String snykToken;


    public ExhortRoutes() {
    }

    @Override
    public void configure() throws Exception {
        from("direct:exhortAnalysis")
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader("Accept", constant("application/json"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/vnd.cyclonedx+json"))
                .setHeader("rhda-token", constant(rhdaToken))
                .setHeader("rhda-source", constant(rhdaSource))
                .setHeader("EXHORT_SNYK_TOKEN", constant(snykToken))
                .setHeader("rhda-operation-type", constant("Component Analysis"))
                .toD(String.format("%s/api/v4/analysis", exhortBackendUrl));
//                .unmarshal()
//                .json(JsonLibrary.Jackson);
    }
}
