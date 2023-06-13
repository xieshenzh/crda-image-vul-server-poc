package org.crda;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.util.List;

@ApplicationScoped
@Path("/image")
public class ImageResource {

    public ImageResource() {
    }

    @Inject
    ImageScraper scraper;

    @GET
    public Image get(@Nonnull @QueryParam("tag") String tag) {
        List<Vulnerability> vulnerabilities = scraper.getVulnerabilities(tag);
//        vulnerabilities.add(new Vulnerability("cve-123", "high"));
//        vulnerabilities.add(new Vulnerability("cve-678", "low"));
        Image image = new Image(tag, vulnerabilities);
        return image;
    }
}
