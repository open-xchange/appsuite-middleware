/**
 * 
 */
package com.openexchange.obs.api;

/**
 * @author choeger
 *
 */
public class BuildServiceException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -7911408031202297775L;

    /**
     * 
     */
    public BuildServiceException() { }

    /**
     * @param message
     */
    public BuildServiceException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public BuildServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public BuildServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
