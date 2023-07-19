package org.crda.sbom;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.builder.RouteBuilder;

@ApplicationScoped
public class SBOMRoutes extends RouteBuilder {

    public SBOMRoutes() {
    }

    @Inject
    SBOMIdempotentRepository idempotentRepository;

    @Override
    public void configure() throws Exception {
        from("direct:getSBOMs")
                .to("direct:parseImageName")
                .to("direct:getImageManifests")
                .to("direct:checkImageFound");
    }
}
