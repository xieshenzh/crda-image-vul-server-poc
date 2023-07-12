package org.crda.image.manifest.model.raw;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class Manifest {

    private String mediaType;
    private String schemaVersion;
    private List<Descriptor> manifests;
    private Descriptor config;

    public Manifest() {
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public List<Descriptor> getManifests() {
        return manifests;
    }

    public void setManifests(List<Descriptor> manifests) {
        this.manifests = manifests;
    }

    public Descriptor getConfig() {
        return config;
    }

    public void setConfig(Descriptor config) {
        this.config = config;
    }
}
