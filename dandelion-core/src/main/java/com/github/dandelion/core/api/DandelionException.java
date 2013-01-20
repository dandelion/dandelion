package com.github.dandelion.core.api;

import java.util.HashMap;
import java.util.Map;

public class DandelionException extends RuntimeException {

    private DandelionError errorCode;
    private Map<String, Object> parameters = new HashMap<String, Object>();


    public DandelionException(DandelionError errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public DandelionException(String message, Throwable exception, DandelionError errorCode) {
        super(message, exception);
        this.errorCode = errorCode;

    }

    public DandelionException set(String field, Object value) {
        parameters.put(field, value);
        return this;
    }

    public <T> T get(String field) {
        return (T) parameters.get(field);
    }

    public DandelionError getErrorCode() {
        return errorCode;
    }

    public static DandelionException wrap(Throwable exception, DandelionError dandelionError) {
        if (exception instanceof DandelionException) {
            DandelionException se = (DandelionException)exception;
            if (dandelionError != null && dandelionError != se.getErrorCode()) {
                return new DandelionException(exception.getMessage(), exception, dandelionError);
            }
            return se;
        } else {
            return new DandelionException(exception.getMessage(), exception, dandelionError);
        }
    }
}
