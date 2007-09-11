

package com.openexchange.admin.rmi.exceptions;


/**
 * Thrown if a database doesn't exist in an operation.
 * 
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class NoSuchDatabaseException extends Exception {

    /**
     * For serialization
     */
    private static final long serialVersionUID = -2871039172296079962L;

    /**
     * 
     */
    public NoSuchDatabaseException() {
        super("Database does not exist");
    }

    /**
     * @param message
     */
    public NoSuchDatabaseException(String message) {
        super(message);

    }

    /**
     * @param cause
     */
    public NoSuchDatabaseException(Throwable cause) {
        super(cause);

    }

    /**
     * @param message
     * @param cause
     */
    public NoSuchDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

}
