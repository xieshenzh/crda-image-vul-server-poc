package org.crda.image;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import static org.crda.image.Constants.imageHeader;
import static org.crda.image.Constants.imageRegRepoHeader;

public class ImageNameProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            String image = exchange.getIn().getHeader(imageHeader, String.class);
            ImageName imageName = new ImageName(image);
            String imageRegistryRepo = imageName.getNameWithoutTag();
            exchange.getIn().setHeader(imageRegRepoHeader, imageRegistryRepo);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
