
package com.openexchange.admin.console;

import java.text.MessageFormat;

public class CLINotFlagException extends CLIUnknownOptionException {

    private static final long serialVersionUID = 6228229421002675543L;

    private final char notflag;

    CLINotFlagException(final String option, final char unflaggish) {
        super(option, MessageFormat.format("Illegal option: ''{0}'', ''{1}'' requires a value", option, Character.valueOf(unflaggish)));
        notflag = unflaggish;
    }

    /**
     * Gets the first character which does not belong to parseable boolean value.
     * 
     * @return The first character which does not belong to parseable boolean value
     */
    public char getOptionChar() {
        return notflag;
    }

}
