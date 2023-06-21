package org.crda.manifest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class Manifest {

    private String mediaType;
    private String schemaVersion;
    private Descriptor[] manifests;
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

    public Descriptor[] getManifests() {
        return manifests;
    }

    public void setManifests(Descriptor[] manifests) {
        this.manifests = manifests;
    }

    public Descriptor getConfig() {
        return config;
    }

    public void setConfig(Descriptor config) {
        this.config = config;
    }
}
