package org.crda.exec;

public class ExecException extends RuntimeException {

    private int code;

    public ExecException() {
    }

    public ExecException(String message, int code) {
        super(message);
        this.code = code;
    }

    public ExecException(String message, int code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ExecException(Throwable cause) {
        super(cause);
    }

    public ExecException(String message, int code, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
