package org.crda.fabric8;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class ImageNameProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            String image = exchange.getIn().getHeader("image", String.class);
            ImageName imageName = new ImageName(image);
            String imageRegistryRepo = imageName.getNameWithoutTag();
            exchange.getIn().setHeader("imageRegRepo", imageRegistryRepo);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
