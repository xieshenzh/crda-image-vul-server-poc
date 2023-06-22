package org.crda.manifest;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class ManifestClientErrorProcessor implements Processor  {
    @Override
    public void process(Exchange exchange) throws Exception {
        int exitCode = exchange.getIn().getHeader("CamelExecExitValue", Integer.class);
        if (exitCode != 0) {
            String errMessage = exchange.getIn().getHeader("CamelExecStdErr", String.class);
            throw new ManifestClientException(errMessage, exitCode);
        }
    }
}
