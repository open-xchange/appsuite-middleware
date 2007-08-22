

package com.openexchange.admin.rmi.exceptions;


/**
 * Is thrown if a filestore doesn't exist in an operation
 * 
 * @author d7
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
