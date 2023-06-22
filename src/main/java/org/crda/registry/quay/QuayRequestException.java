package org.crda.registry.quay;

public class QuayRequestException extends RuntimeException {

    private int code;

    public QuayRequestException() {
    }

    public QuayRequestException(String message, int code) {
        super(message);
        this.code = code;
    }

    public QuayRequestException(String message, int code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public QuayRequestException(Throwable cause) {
        super(cause);
    }

    public QuayRequestException(String message, int code, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
