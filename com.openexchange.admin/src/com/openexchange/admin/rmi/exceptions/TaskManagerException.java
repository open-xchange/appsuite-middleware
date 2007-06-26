package com.openexchange.admin.rmi.exceptions;

public class TaskManagerException extends Exception {

    /**
     * For serialization
     */
    private static final long serialVersionUID = -1523044898153023473L;

    /**
     * 
     */
    public TaskManagerException() {

    }

    /**
     * @param message
     */
    public TaskManagerException(String message) {
        super(message);

    }

    /**
     * @param cause
     */
    public TaskManagerException(Throwable cause) {
        super(cause);

    }

    /**
     * @param message
     * @param cause
     */
    public TaskManagerException(String message, Throwable cause) {
        super(message, cause);
    }

}
