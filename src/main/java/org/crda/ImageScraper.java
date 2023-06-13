package org.crda;

import com.github.dockerjava.api.DockerClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class ImageScraper {

    @Inject
    DockerClient dockerClient;

    List<Vulnerability> getVulnerabilities(String tag) {
        dockerClient.listImagesCmd().exec();
        return Collections.emptyList();
    }
}
