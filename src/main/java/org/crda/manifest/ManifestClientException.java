package org.crda.manifest;

public class ManifestClientException extends RuntimeException {

    private int code;

    public ManifestClientException() {
    }

    public ManifestClientException(String message, int code) {
        super(message);
        this.code = code;
    }

    public ManifestClientException(String message, int code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ManifestClientException(Throwable cause) {
        super(cause);
    }

    public ManifestClientException(String message, int code, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
