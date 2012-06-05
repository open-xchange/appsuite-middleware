
package com.openexchange.admin.lib.console;

/**
 * {@link CLIOptionException} - Base class for CLI parsing error
 */
public abstract class CLIOptionException extends Exception {

    private static final long serialVersionUID = 7125577259431666541L;

    /**
     * Initializes a new {@link CLIOptionException}.
     * 
     * @param msg The error message
     */
    CLIOptionException(final String msg) {
        super(msg);
    }

    /**
     * Initializes a new {@link CLIOptionException}.
     * 
     * @param msg The error message
     * @param cause The cause
     */
    CLIOptionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
