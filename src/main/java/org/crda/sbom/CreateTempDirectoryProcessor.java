package org.crda.sbom;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.crda.image.Constants.imagePathHeader;
import static org.crda.image.Constants.imageRefHeader;

public class CreateTempDirectoryProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String imageRef = exchange.getIn().getHeader(imageRefHeader, String.class);
        String prefix = imageRef.replaceAll("[\\\\/:*?%\"<>|]", "_");
        try {
            Path tmpPath = Files.createTempDirectory(prefix);
            tmpPath.toFile().deleteOnExit();
            ;
            exchange.getIn().setHeader(imagePathHeader, tmpPath.toFile().getAbsolutePath());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
