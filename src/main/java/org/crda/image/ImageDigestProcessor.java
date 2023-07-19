package org.crda.image;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import static org.crda.image.Constants.imageRegRepoHeader;

public class ImageDigestProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String registryRepo = exchange.getIn().getHeader(imageRegRepoHeader, String.class);
        String digest = exchange.getIn().getBody(String.class);
        exchange.getIn().setBody(registryRepo + "@" + digest);
    }
}
