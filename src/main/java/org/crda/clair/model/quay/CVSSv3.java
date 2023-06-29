package org.crda.clair.model.quay;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class CVSSv3 {
    private String vectors;
    private String score;

    public CVSSv3() {
    }

    public String getVectors() {
        return vectors;
    }

    public void setVectors(String vectors) {
        this.vectors = vectors;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
