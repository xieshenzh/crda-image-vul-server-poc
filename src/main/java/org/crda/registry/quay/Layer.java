package org.crda.registry.quay;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class Layer {
    private String name;
    private String namespaceName;
    private String parentName;
    private int indexedByVersion;
    private List<Feature> features;

    public Layer() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public int getIndexedByVersion() {
        return indexedByVersion;
    }

    public void setIndexedByVersion(int indexedByVersion) {
        this.indexedByVersion = indexedByVersion;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}
