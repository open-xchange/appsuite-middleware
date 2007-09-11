

package com.openexchange.admin.rmi.exceptions;


/**
 * Thrown if an user doesn't exist in an operation.
 * 
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
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
