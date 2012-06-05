
package com.openexchange.admin.lib.console;

import java.text.MessageFormat;

/**
 * {@link CLIIllegalOptionValueException} - Indicates an illegal or missing value.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CLIIllegalOptionValueException extends CLIOptionException {

    private static final long serialVersionUID = 9131675571912299169L;

    private final CLIOption option;

    private final String value;

    public CLIIllegalOptionValueException(final CLIOption opt, final String value) {
        this(opt, value, null);
    }

    public CLIIllegalOptionValueException(final CLIOption opt, final String value, final Throwable cause) {
        super(MessageFormat.format(
            "Illegal value ``{0}'' for option {1}--{2}",
            value,
            (opt.shortForm() == null ? "" : "-" + opt.shortForm() + "/"),
            opt.longForm()), cause);
        this.option = opt;
        this.value = value;
    }

    /**
     * Gets the name of the option whose value was illegal.
     * 
     * @return The name of the option whose value was illegal (e.g. "-u")
     */
    public CLIOption getOption() {
        return this.option;
    }

    /**
     * Gets the illegal value.
     * 
     * @return The illegal value
     */
    public String getValue() {
        return this.value;
    }

}
