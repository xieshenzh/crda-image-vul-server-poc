package org.crda.sbom.cyclone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.builder.RouteBuilder;
import org.cyclonedx.model.Bom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class CycloneRoutes extends RouteBuilder {

    public CycloneRoutes() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CycloneRoutes.class);

    private static final Set<String> SUPPORTED_TYPES = new HashSet<>();

    static {
        SUPPORTED_TYPES.add("maven");
        SUPPORTED_TYPES.add("npm");
        SUPPORTED_TYPES.add("pypi");
        SUPPORTED_TYPES.add("golang");
        SUPPORTED_TYPES.add("deb");
        SUPPORTED_TYPES.add("rpm");
        SUPPORTED_TYPES.add("gradle");
    }

    @Inject
    ObjectMapper mapper;

    @Override
    public void configure() throws Exception {

        from("direct:splitSBOM")
                .process(exchange -> {
                    JsonNode node = exchange.getIn().getBody(JsonNode.class);

                    TokenBuffer buffer = new TokenBuffer(mapper, false);
                    mapper.writeValue(buffer, node);
                    Bom bom = mapper.readValue(buffer.asParser(), Bom.class);

                    Set<String> types = bom.getComponents()
                            .stream()
                            .map(c -> {
                                try {
                                    return new PackageURL(c.getPurl()).getType();
                                } catch (MalformedPackageURLException e) {
                                    LOGGER.warn("Failed to parse component purl {} in SBOM", c.getPurl());
                                    // Ignore this component if its purl is not valid
                                    return null;
                                }
                            })
                            .filter(SUPPORTED_TYPES::contains)
                            .collect(Collectors.toSet());

                    List<Bom> boms = types.stream()
                            .map(t -> {
                                Bom b;
                                try {
                                    b = mapper.readValue(buffer.asParser(), Bom.class);
                                } catch (IOException e) {
                                    LOGGER.warn("Failed to generate SBOM object");
                                    return null;
                                }

                                b.setComponents(
                                        bom.getComponents()
                                                .stream()
                                                .filter(c -> {
                                                    try {
                                                        return t.equals(new PackageURL(c.getPurl()).getType());
                                                    } catch (MalformedPackageURLException e) {
                                                        LOGGER.warn("Failed to parse component purl {} in SBOM", c.getPurl());
                                                        // Ignore this component if its purl is not valid
                                                        return false;
                                                    }
                                                })
                                                .collect(Collectors.toList())
                                );

                                b.setDependencies(
                                        bom.getDependencies()
                                                .stream()
                                                .filter(d -> {
                                                    try {
                                                        return t.equals(new PackageURL(d.getRef()).getType());
                                                    } catch (MalformedPackageURLException e) {
                                                        LOGGER.warn("Failed to parse dependency purl {} in SBOM", d.getRef());
                                                        // Ignore this component if its purl is not valid
                                                        return false;
                                                    }
                                                })
                                                .collect(Collectors.toList())
                                );

                                return b;
                            })
                            .toList();

                    exchange.getIn().setBody(boms);
                });
    }
}
