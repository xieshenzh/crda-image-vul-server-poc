package org.crda.image.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ImageRef {

    String registry;
    String repository;
    String tag;
    String digest;

    public ImageRef() {

    }

    public ImageRef(String registry, String repository, String tag, String digest) {
        this.registry = registry;
        this.repository = repository;
        this.tag = tag;
        this.digest = digest;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }
}
