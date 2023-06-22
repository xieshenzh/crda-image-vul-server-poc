package org.crda.registry.quay;

public class QuayRequestException extends RuntimeException {
    public QuayRequestException() {
    }

    public QuayRequestException(String message) {
        super(message);
    }

    public QuayRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuayRequestException(Throwable cause) {
        super(cause);
    }

    public QuayRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
