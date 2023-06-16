package org.crda.image;

public class InvalidImageRefException extends IllegalArgumentException {
    public InvalidImageRefException() {
    }

    public InvalidImageRefException(String s) {
        super(s);
    }

    public InvalidImageRefException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidImageRefException(Throwable cause) {
        super(cause);
    }
}
