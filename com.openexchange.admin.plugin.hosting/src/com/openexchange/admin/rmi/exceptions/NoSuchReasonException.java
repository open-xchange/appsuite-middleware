

package com.openexchange.admin.rmi.exceptions;


/**
 * Is thrown if a user doesn't exist in an operation
 * 
 * @author d7
 *
 */
public class NoSuchReasonException extends Exception {

    /**
     * For serializations
     */
    private static final long serialVersionUID = 8838129017619256228L;

    /**
     * 
     */
    public NoSuchReasonException() {
        super("Reason does not exist");
    }

    /**
     * @param message
     */
    public NoSuchReasonException(String message) {
        super(message);

    }

    /**
     * @param cause
     */
    public NoSuchReasonException(Throwable cause) {
        super(cause);

    }

    /**
     * @param message
     * @param cause
     */
    public NoSuchReasonException(String message, Throwable cause) {
        super(message, cause);
    }

}
