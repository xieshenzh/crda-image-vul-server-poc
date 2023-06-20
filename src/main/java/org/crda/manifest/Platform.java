package org.crda.manifest;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Platform {
    private  String architecture;
    private   String os;
    private   String osVersion;
    private  String osFeatures;
    private   String variant;
    private  String[] features;

    public Platform() {
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsFeatures() {
        return osFeatures;
    }

    public void setOsFeatures(String osFeatures) {
        this.osFeatures = osFeatures;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String[] getFeatures() {
        return features;
    }

    public void setFeatures(String[] features) {
        this.features = features;
    }
}
