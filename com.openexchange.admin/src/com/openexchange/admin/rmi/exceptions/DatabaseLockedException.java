package com.openexchange.admin.rmi.exceptions;

import java.io.Serializable;

/**
 * This exception is thrown when the underlying database is locked due to an update or so
 * 
 * @author dsieben
 *
 */
public class DatabaseLockedException extends Exception implements Serializable {

    /**
     * For serialization
     */
    private static final long serialVersionUID = -3944701775507953137L;

    /**
     * Default constructor
     */
    public DatabaseLockedException() {

    }

    /**
     * @param message
     */
    public DatabaseLockedException(String message) {
        super(message);

    }

    /**
     * @param cause
     */
    public DatabaseLockedException(Throwable cause) {
        super(cause);

    }

    /**
     * @param message
     * @param cause
     */
    public DatabaseLockedException(String message, Throwable cause) {
        super(message, cause);
    }

}
