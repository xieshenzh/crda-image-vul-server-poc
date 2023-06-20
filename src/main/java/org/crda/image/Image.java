package org.crda.image;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public class Image {

    private String ref;
    private List<Vulnerability> vulnerabilities;

    public Image() {

    }

    public Image(String ref, List<Vulnerability> vulnerabilities) {
        this.ref = ref;
        this.vulnerabilities = vulnerabilities;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public List<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }
}
