/**
 * 
 */
package com.openexchange.admin.rmi.exceptions;

/**
 * @author choeger
 *
 */
public class EnforceableDataObjectException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -7692303400774923314L;

    /**
     * 
     */
    public EnforceableDataObjectException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public EnforceableDataObjectException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public EnforceableDataObjectException(String message) {
        super("EnforceableDataObjectException: " + message);
    }

    /**
     * @param cause
     */
    public EnforceableDataObjectException(Throwable cause) {
        super(cause);
    }

}
