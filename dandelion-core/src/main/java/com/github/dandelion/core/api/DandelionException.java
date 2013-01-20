package com.github.dandelion.core.api;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Exception for Dandelion domain
 */
public class DandelionException extends RuntimeException {
    /**
     * Error Code of this Exception
     */
    private DandelionError errorCode;
    /**
     * Parameters of this Exception
     */
    private Map<String, Object> parameters = new LinkedHashMap<String, Object>();

    /**
     * @param errorCode Domain-Specific Error
     */
    public DandelionException(DandelionError errorCode) {
        super();
        this.errorCode = errorCode;
    }

    /**
     * Create the Bundle Key for a Dandelion Error
     * @param error Dandelion Error
     * @return a valid Bundle Key
     */
    public static String createBundleKey(DandelionError error) {
        return error.getClass().getSimpleName() + "__" + error.getNumber();
    }

    /**
     * Create a DandelionException to wrapped Exception
     * @param message message of wrapped exception
     * @param exception wrapped exception
     * @param errorCode associated error
     */
    private DandelionException(String message, Throwable exception, DandelionError errorCode) {
        super(message, exception);
        this.errorCode = errorCode;

    }

    /**
     * @return associated error to this exception
     */
    public DandelionError getErrorCode() {
        return errorCode;
    }

    /**
     * Add a relevant data (field/value) for this exception
     * @param field field of this relevant data
     * @param value value of this relevant data
     * @return this exception (for 'fluent interface' purpose)
     */
    public DandelionException set(String field, Object value) {
        parameters.put(field, value);
        return this;
    }

    /**
     * Get the value of stored data by his field
     * @param field field of stored data
     * @param <T> type of this value
     * @return the casted value
     */
    public <T> T get(String field) {
        return (T) parameters.get(field);
    }

    /**
     * Wrap a Exception into a DandelionException
     * @param exception exception to wrap
     * @param error associated dandelion error
     * @return generated exception
     */
    public static DandelionException wrap(Throwable exception, DandelionError error) {
        if (exception instanceof DandelionException) {
            DandelionException se = (DandelionException)exception;
            if (error != null && error != se.getErrorCode()) {
                return new DandelionException(exception.getMessage(), exception, error);
            }
            return se;
        } else {
            return new DandelionException(exception.getMessage(), exception, error);
        }
    }

    /**
     * @return the localized Message for Dandelion Error
     */
    @Override
    public String getLocalizedMessage() {
        if (errorCode == null) {
            return null;
        }
        String key = createBundleKey(errorCode);
        ResourceBundle bundle = ResourceBundle.getBundle("com.github.dandelion.core.api.exceptions");
        return MessageFormat.format(bundle.getString(key), parameters.values());
    }
}
