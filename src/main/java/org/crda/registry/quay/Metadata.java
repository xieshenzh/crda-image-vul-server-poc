package org.crda.registry.quay;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class Metadata {
    private String updatedBy;
    private String repoName;
    private String repoLink;
    private String distroName;
    private String distroVersion;
    private NVD NVD;

    public Metadata() {
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoLink() {
        return repoLink;
    }

    public void setRepoLink(String repoLink) {
        this.repoLink = repoLink;
    }

    public String getDistroName() {
        return distroName;
    }

    public void setDistroName(String distroName) {
        this.distroName = distroName;
    }

    public String getDistroVersion() {
        return distroVersion;
    }

    public void setDistroVersion(String distroVersion) {
        this.distroVersion = distroVersion;
    }

    public org.crda.registry.quay.NVD getNVD() {
        return NVD;
    }

    public void setNVD(org.crda.registry.quay.NVD NVD) {
        this.NVD = NVD;
    }
}
