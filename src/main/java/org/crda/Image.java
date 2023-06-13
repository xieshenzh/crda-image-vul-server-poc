package org.crda;

import java.util.List;

public class Image {

    private String tag;
    private List<Vulnerability> vulnerabilities;

    public Image(String tag, List<Vulnerability> vulnerabilities) {
        this.tag = tag;
        this.vulnerabilities = vulnerabilities;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }
}
