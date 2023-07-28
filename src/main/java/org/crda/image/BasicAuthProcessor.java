package org.crda.image;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.crda.sbom.Constants.*;

public class BasicAuthProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String authStr = exchange.getProperty(authProperty, String.class);

        try {
            if (authStr != null && authStr.startsWith("Basic")) {
                String credBase64Str = authStr.substring("Basic".length()).trim();
                byte[] cred = Base64.getDecoder().decode(credBase64Str);
                String credStr = new String(cred, StandardCharsets.UTF_8);
                exchange.getIn().setHeader(credsHeader, credStr);
            }
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("The provided auth credentials are invalid.");
        }
    }
}
