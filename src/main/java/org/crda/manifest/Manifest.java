package org.crda.manifest;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Manifest {

    private String mediaType;
    private String schemaVersion;
    private String[] signatures;
    private Descriptor[] manifests;
    private Descriptor[] layers;

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

    public String[] getSignatures() {
        return signatures;
    }

    public void setSignatures(String[] signatures) {
        this.signatures = signatures;
    }

    public Descriptor[] getManifests() {
        return manifests;
    }

    public void setManifests(Descriptor[] manifests) {
        this.manifests = manifests;
    }

    public Descriptor[] getLayers() {
        return layers;
    }

    public void setLayers(Descriptor[] layers) {
        this.layers = layers;
    }
}
