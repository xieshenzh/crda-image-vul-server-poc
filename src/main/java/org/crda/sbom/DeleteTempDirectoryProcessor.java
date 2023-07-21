package org.crda.sbom;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.crda.image.Constants.imagePathHeader;

public class DeleteTempDirectoryProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String dir = exchange.getIn().getHeader(imagePathHeader, String.class);
        Path dirPath = Paths.get(dir);
        try (Stream<Path> stream = Files.walk(dirPath)) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        if (Files.exists(dirPath)) {
            throw new RuntimeException(String.format("Failed to delete temp directory: %s", dir));
        }
    }
}
