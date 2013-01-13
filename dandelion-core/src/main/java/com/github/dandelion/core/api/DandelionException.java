package com.github.dandelion.core.api;

/**
 * Exception thrown when Flyway encounters a problem.
 */
public class DandelionException extends RuntimeException {

	private static final long serialVersionUID = -8260630442398315304L;

	/**
     * Creates a new DandelionException with this message and this cause.
     *
     * @param message The exception message.
     * @param cause   The exception cause.
     */
    public DandelionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new DandelionException with this message.
     *
     * @param message The exception message.
     */
    public DandelionException(String message) {
        super(message);
    }

    /**
     * Creates a new DandelionException. For use in subclasses that override getMessage().
     */
    public DandelionException() {
        super();
    }
}
