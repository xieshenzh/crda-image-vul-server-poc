package org.crda.manifest;

public class ManifestClientException extends RuntimeException {
    public ManifestClientException() {
    }

    public ManifestClientException(String message) {
        super(message);
    }

    public ManifestClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManifestClientException(Throwable cause) {
        super(cause);
    }

    public ManifestClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
