package org.crda.exec;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.exec.ExecBinding;

public class ExecErrorProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        int exitCode = exchange.getIn().getHeader(ExecBinding.EXEC_EXIT_VALUE, Integer.class);
        if (exitCode != 0) {
            String errMessage = exchange.getIn().getHeader(ExecBinding.EXEC_STDERR, String.class);
            throw new RuntimeException(errMessage);
        }
    }
}
