package org.crda.registry.quay;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class NVD {
    private CVSSv3 CVSSv3;

    public NVD() {
    }

    public org.crda.registry.quay.CVSSv3 getCVSSv3() {
        return CVSSv3;
    }

    public void setCVSSv3(org.crda.registry.quay.CVSSv3 CVSSv3) {
        this.CVSSv3 = CVSSv3;
    }
}
