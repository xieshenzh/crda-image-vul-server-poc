package org.crda.vul.exhort;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.vertxHttp;

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
        from(direct("exhortAnalysis"))
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader("Accept", constant("application/json"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/vnd.cyclonedx+json"))
                .setHeader("rhda-token", constant(rhdaToken))
                .setHeader("rhda-source", constant(rhdaSource))
                .setHeader("EXHORT_SNYK_TOKEN", constant(snykToken))
                .setHeader("rhda-operation-type", constant("Component Analysis"))
                .to(vertxHttp(String.format("%s/api/v4/analysis", exhortBackendUrl)))
                .unmarshal()
                .json(JsonLibrary.Jackson);
    }
}
