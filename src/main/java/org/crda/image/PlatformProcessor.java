package org.crda.image;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.crda.image.manifest.model.raw.Platform;

import static org.crda.image.Constants.platformHeader;
import static org.crda.image.Constants.supportedPlatforms;

public class PlatformProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String platformStr = exchange.getIn().getHeader(platformHeader, String.class);
        if (platformStr != null) {
            Platform platform = new Platform(platformStr);
            if (!supportedPlatforms.contains(platform)) {
                throw new IllegalArgumentException(String.format("Platform %s is not supported", platformStr));
            }
        }
    }
}
