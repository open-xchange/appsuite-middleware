package com.openexchange.admin.rmi.exceptions;

public class DuplicateExtensionException extends Exception {

    /**
     * For serialization
     */
    private static final long serialVersionUID = -8166971510995800360L;

    /**
     * @param message
     */
    public DuplicateExtensionException(final String extname) {
        super("An extension named " + extname + " is already registered");

    }

    /**
     * @param cause
     */
    public DuplicateExtensionException(final Throwable cause) {
        super(cause);

    }

    /**
     * @param message
     * @param cause
     */
    public DuplicateExtensionException(final String message, final Throwable cause) {
        super(message, cause);
    }


}
