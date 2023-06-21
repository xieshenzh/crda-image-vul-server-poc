package org.crda.registry;

public class RegistryUnsupportedException extends RuntimeException {
    public RegistryUnsupportedException() {
    }

    public RegistryUnsupportedException(String message) {
        super(message);
    }

    public RegistryUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegistryUnsupportedException(Throwable cause) {
        super(cause);
    }

    public RegistryUnsupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
