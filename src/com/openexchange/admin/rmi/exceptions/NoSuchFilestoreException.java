

package com.openexchange.admin.rmi.exceptions;



/**
 * Thrown if a filestore doesn't exist in an operation
 * 
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class NoSuchFilestoreException extends Exception {

    /**
     * For serialization
     */
    private static final long serialVersionUID = 5261789028162979524L;

    /**
     * 
     */
    public NoSuchFilestoreException() {
        super("Filestore does not exist");
    }

    /**
     * @param message
     */
    public NoSuchFilestoreException(String message) {
        super(message);

    }

    /**
     * @param cause
     */
    public NoSuchFilestoreException(Throwable cause) {
        super(cause);

    }

    /**
     * @param message
     * @param cause
     */
    public NoSuchFilestoreException(String message, Throwable cause) {
        super(message, cause);
    }

}
