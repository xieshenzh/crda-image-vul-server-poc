package org.crda;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class ImageDigestProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String registryRepo = exchange.getIn().getHeader("imageRegRepo", String.class);
        String digest = exchange.getIn().getBody(String.class);
        exchange.getIn().setBody(registryRepo + "@" + digest);
    }
}
