package org.crda.cache;

import org.apache.camel.Converter;
import org.crda.image.vulnerability.model.quay.Feature;
import org.crda.image.vulnerability.model.quay.Secscan;
import org.crda.cache.model.Image;
import org.crda.cache.model.Vulnerability;

import java.util.*;
import java.util.stream.Collectors;

@Converter
public class ImageConverter {

    public ImageConverter() {
    }

    @Converter
    public static Image toInputStream(Secscan result) {
        Map<String, Vulnerability> vulnerabilityMap =
                "scanned".equals(result.getStatus())
                        && result.getData() != null
                        && result.getData().getLayer() != null
                        && result.getData().getLayer().getFeatures() != null ?
                        result.getData().getLayer().getFeatures()
                                .stream()
                                .map(Feature::getVulnerabilities)
                                .filter(Objects::nonNull)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toMap(org.crda.image.vulnerability.model.quay.Vulnerability::getName,
                                        v -> new Vulnerability(v.getName(), v.getSeverity()),
                                        (k1, k2) -> k1)) :
                        Collections.emptyMap();

        Image image = new Image();
        image.setDigest(result.getData().getLayer().getName());
        image.setVulnerabilities(new ArrayList<>(vulnerabilityMap.values()));
        return image;
    }
}
