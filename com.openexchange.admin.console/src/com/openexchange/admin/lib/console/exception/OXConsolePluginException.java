package com.openexchange.admin.lib.console.exception;


public class OXConsolePluginException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 5743756641717340398L;

    /**
     * Initializes a new {@link OXConsolePluginException}.
     */
    public OXConsolePluginException() {
        super();
    }

    /**
     * Initializes a new {@link OXConsolePluginException}.
     * @param message
     * @param cause
     */
    public OXConsolePluginException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes a new {@link OXConsolePluginException}.
     * @param message
     */
    public OXConsolePluginException(final String message) {
        super(message);
    }

    /**
     * Initializes a new {@link OXConsolePluginException}.
     * @param cause
     */
    public OXConsolePluginException(final Throwable cause) {
        super(cause);
    }

    
}
