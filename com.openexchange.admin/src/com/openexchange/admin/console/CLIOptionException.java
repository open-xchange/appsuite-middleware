
package com.openexchange.admin.console;

/**
 * {@link CLIOptionException} - Base class for CLI parsing error
 */
public abstract class CLIOptionException extends Exception {

    private static final long serialVersionUID = 7125577259431666541L;

    CLIOptionException(final String msg) {
        super(msg);
    }

    CLIOptionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
