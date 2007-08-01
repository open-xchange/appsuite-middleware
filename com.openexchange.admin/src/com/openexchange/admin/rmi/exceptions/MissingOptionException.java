package com.openexchange.admin.rmi.exceptions;

public class MissingOptionException extends Exception {
    /**
     * For serialization
     */
    private static final long serialVersionUID = 8134438398224308263L;

    public MissingOptionException(final String msg) {
        super(msg);
    }
}
