
package com.openexchange.admin.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * {@link CLIParser} - The command-line parser.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CLIParser {

    private String[] remainingArgs;

    private final Map<String, CLIOption> options = new HashMap<String, CLIOption>(16);

    private final Map<String, List<Object>> values = new HashMap<String, List<Object>>(16);

    private final Options cliOptions;

    private CommandLine cliCommandLine;

    /**
     * Initializes a new {@link CLIParser}.
     */
    public CLIParser() {
        super();
        cliOptions = new Options();
    }

    /**
     * Adds the specified option to the list of options
     * 
     * @param opt The option to add
     * @return The added option
     */
    public CLIOption addOption(final CLIOption opt) {
        final String shortForm = opt.shortForm();
        if (shortForm != null) {
            this.options.put("-" + shortForm, opt);
        }
        final String longForm = opt.longForm();
        this.options.put("--" + longForm, opt);

        cliOptions.addOption(shortForm, longForm, opt.wantsValue(), "");

        return opt;
    }

    /**
     * Convenience method for adding a string option.
     * 
     * @param shortForm The option's short form
     * @param longForm The option's long form
     * @return The added option
     */
    public CLIOption addStringOption(final char shortForm, final String longForm) {
        return addOption(new CLIOption.CLIStringOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a string option.
     * 
     * @param longForm The option's long form
     * @return The added option
     */
    public CLIOption addStringOption(final String longForm) {
        return addOption(new CLIOption.CLIStringOption(longForm));
    }

    /**
     * Convenience method for adding a settable boolean option.
     * 
     * @param longForm The option's long form
     * @return The added option
     */
    public CLIOption addSettableBooleanOption(final String longForm) {
        return addOption(new CLIOption.CLISettableBooleanOption(longForm));
    }

    /**
     * Convenience method for adding an integer option.
     * 
     * @param shortForm The option's short form
     * @param longForm The option's long form
     * @return The added option
     */
    public CLIOption addIntegerOption(final char shortForm, final String longForm) {
        return addOption(new CLIOption.CLIIntegerOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding an integer option.
     * 
     * @param longForm The option's long form
     * @return The added option
     */
    public CLIOption addIntegerOption(final String longForm) {
        return addOption(new CLIOption.CLIIntegerOption(longForm));
    }

    /**
     * Convenience method for adding a long integer option.
     * 
     * @param shortForm The option's short form
     * @param longForm The option's long form
     * @return The added option
     */
    public CLIOption addLongOption(final char shortForm, final String longForm) {
        return addOption(new CLIOption.CLILongOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a long integer option.
     * 
     * @param longForm The option's long form
     * @return The added option
     */
    public CLIOption addLongOption(final String longForm) {
        return addOption(new CLIOption.CLILongOption(longForm));
    }

    /**
     * Convenience method for adding a double option.
     * 
     * @param shortForm The option's short form
     * @param longForm The option's long form
     * @return The added option
     */
    public CLIOption addDoubleOption(final char shortForm, final String longForm) {
        return addOption(new CLIOption.CLIDoubleOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a double option.
     * 
     * @param longForm The option's long form
     * @return The added option
     */
    public CLIOption addDoubleOption(final String longForm) {
        return addOption(new CLIOption.CLIDoubleOption(longForm));
    }

    /**
     * Convenience method for adding a boolean option.
     * 
     * @param shortForm The option's short form
     * @param longForm The option's long form
     * @return The added option
     */
    public CLIOption addBooleanOption(final char shortForm, final String longForm) {
        return addOption(new CLIOption.CLIBooleanOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a boolean option.
     * 
     * @param longForm The option's long form
     * @return The added option
     */
    public CLIOption addBooleanOption(final String longForm) {
        return addOption(new CLIOption.CLIBooleanOption(longForm));
    }

    /**
     * Equivalent to {@link #getOptionValue(Option, Object) getOptionValue(o, null)}.
     */
    public Object getOptionValue(final CLIOption o) {
        return getOptionValue(o, null, false);
    }

    /**
     * Gets given option's value.
     * 
     * @return the parsed value of the given option, or <code>null</code> if the option was not set
     */
    public Object getOptionValue(final CLIOption o, final Object def, final boolean remove) {
        if (null == cliCommandLine) {
            throw new IllegalStateException("Command line has not been parsed, yet.");
        }

        final List<Object> vals = values.get(o.longForm());
        if (null == vals) {
            return def;
        }

        if (vals.isEmpty()) {
            return null;
        }

        return remove ? vals.remove(0) : vals.get(0);
    }

    /**
     * @return the non-option arguments
     */
    public String[] getRemainingArgs() {
        return remainingArgs;
    }

    /**
     * Removes given option.
     * 
     * @param option The option to remove
     */
    protected final void removeOption(final CLIOption option) {
        final String shortForm = option.shortForm();
        if (null != shortForm) {
            this.options.remove("-" + shortForm);
        }
        this.options.remove("--" + option.longForm());
    }

    /**
     * Parses specified command line.
     * 
     * @param argv The command line
     * @throws CLIParseException If parsing fails
     * @throws CLIIllegalOptionValueException If an illegal option occurs
     */
    public void parse(final String[] argv) throws CLIParseException, CLIIllegalOptionValueException {
        parse(argv, Locale.getDefault());
    }

    private static interface OptionProvider {

        boolean hasOption();

        String getValue();
    }

    private static final class ShortOptionProvider implements OptionProvider {

        private final CommandLine cliCommandLine;

        private char c;

        ShortOptionProvider(final char c, final CommandLine cliCommandLine) {
            super();
            this.c = c;
            this.cliCommandLine = cliCommandLine;
        }

        void set(final char c) {
            this.c = c;
        }

        public String getValue() {
            return cliCommandLine.getOptionValue(c);
        }

        public boolean hasOption() {
            return cliCommandLine.hasOption(c);
        }

    }

    private static final class LongOptionProvider implements OptionProvider {

        private final CommandLine cliCommandLine;

        private String s;

        LongOptionProvider(final String s, final CommandLine cliCommandLine) {
            super();
            this.s = s;
            this.cliCommandLine = cliCommandLine;
        }

        void set(final String s) {
            this.s = s;
        }

        public String getValue() {
            return cliCommandLine.getOptionValue(s);
        }

        public boolean hasOption() {
            return cliCommandLine.hasOption(s);
        }

    }

    /**
     * Parses specified command line.
     * 
     * @param argv The command line
     * @param locale The locale
     * @throws CLIParseException If parsing fails
     * @throws CLIIllegalOptionValueException If an illegal option occurs
     */
    public void parse(final String[] argv, final Locale locale) throws CLIParseException, CLIIllegalOptionValueException {
        try {
            cliCommandLine = new PosixParser().parse(cliOptions, argv);

            LongOptionProvider lp = null;
            ShortOptionProvider sp = null;

            for (final CLIOption opt : new HashSet<CLIOption>(options.values())) {
                final String shortForm = opt.shortForm();
                if (null == shortForm) {
                    if (null == lp) {
                        lp = new LongOptionProvider(opt.longForm(), cliCommandLine);
                    } else {
                        lp.set(opt.longForm());
                    }
                    handleOption(opt, shortForm, locale, lp);
                } else {
                    if (null == sp) {
                        sp = new ShortOptionProvider(shortForm.charAt(0), cliCommandLine);
                    } else {
                        sp.set(shortForm.charAt(0));
                    }
                    handleOption(opt, shortForm, locale, sp);
                }
            }
        } catch (final ParseException e) {
            throw new CLIParseException(argv, e);
        }
    }

    private void handleOption(final CLIOption opt, final String shortForm, final Locale locale, final OptionProvider provider) throws CLIIllegalOptionValueException {
        if (provider.hasOption()) {
            final String val = provider.getValue();
            final String longForm = opt.longForm();

            List<Object> vals = values.get(longForm);
            if (null == vals) {
                vals = new ArrayList<Object>(2);
                values.put(longForm, vals);
                if (null != shortForm) {
                    values.put(shortForm, vals);
                }
            }

            vals.add(opt.parseValue(val, locale));

        } else if (opt.wantsValue()) {
            throw new CLIIllegalOptionValueException(opt, "");
        }
    }

}
