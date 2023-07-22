package org.crda.image.manifest.model.raw;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Objects;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class Platform {
    private String architecture;
    private String os;

    public Platform() {
    }

    public Platform(String platform) {
        String[] parts = platform.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException(String.format("Invalid platform parameter %s", platform));
        }
        this.os = parts[0];
        this.architecture = parts[1];
    }

    public Platform os(String os) {
        this.os = os;
        return this;
    }

    public Platform arch(String arch) {
        this.architecture = arch;
        return this;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Platform platform = (Platform) o;
        return Objects.equals(architecture, platform.architecture) && Objects.equals(os, platform.os);
    }

    @Override
    public int hashCode() {
        return Objects.hash(architecture, os);
    }
}
