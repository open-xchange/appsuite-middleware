package com.openexchange.admin.rmi.exceptions;

public class NoSuchPluginException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -3509196124610032292L;

    /**
     * 
     */
    public NoSuchPluginException() {
            super("Such a plugin doesn't exist");
    }

    /**
     * @param message
     */
    public NoSuchPluginException(final String message) {
            super("Such a plugin doesn't exist: " + message);
            
    }

    /**
     * @param cause
     */
    public NoSuchPluginException(final Throwable cause) {
            super(cause);
            
    }

    /**
     * @param message
     * @param cause
     */
    public NoSuchPluginException(final String message, final Throwable cause) {
            super(message, cause);          
    }

}
