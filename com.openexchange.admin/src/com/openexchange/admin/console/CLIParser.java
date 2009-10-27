
package com.openexchange.admin.console;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;

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
     * Adds the specified option to the list of options.
     * 
     * @param opt The option to add
     * @return The added option
     */
    public CLIOption addOption(final CLIOption opt) {
        final String shortForm = opt.shortForm();
        final StringBuilder sb = new StringBuilder(16);
        final String sopt;
        if (shortForm == null) {
            sopt = " "; // Whitespace
        } else {
            this.options.put(sb.append('-').append(shortForm).toString(), opt);
            sb.setLength(0);
            sopt = shortForm;
        }
        final String longForm = opt.longForm();
        this.options.put(sb.append("--").append(longForm).toString(), opt);

        cliOptions.addOption(sopt, longForm, opt.wantsValue(), "");

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
            return def;
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
     * Gets the parsed values of all occurrences of given option, or an empty collection if the option was not set.
     * 
     * @param option The option
     * @return The parsed values of all occurrences of given option
     */
    public final Collection<Object> getOptionValues(final CLIOption option) {
        final List<Object> result = new ArrayList<Object>();

        while (true) {
            final Object o = getOptionValue(option, null, true);

            if (o == null) {
                return result;
            }

            result.add(o);
        }
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
        final StringBuilder sb = new StringBuilder(16);
        if (null != shortForm) {
            this.options.remove(sb.append('-').append(shortForm).toString());
            sb.setLength(0);
        }
        this.options.remove(sb.append("--").append(option.longForm()).toString());
    }

    /**
     * Parses specified command line.
     * 
     * @param argv The command line arguments
     * @throws CLIParseException If parsing fails
     * @throws CLIIllegalOptionValueException If an illegal option occurs
     * @throws CLIUnknownOptionException If an unknown option occurs
     */
    public void parse(final String[] argv) throws CLIParseException, CLIIllegalOptionValueException, CLIUnknownOptionException {
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
     * @param argv The command line arguments
     * @param locale The locale
     * @throws CLIParseException If parsing fails
     * @throws CLIIllegalOptionValueException If an illegal option occurs
     * @throws CLIUnknownOptionException If an unknown option occurs
     */
    public void parse(final String[] argv, final Locale locale) throws CLIParseException, CLIIllegalOptionValueException, CLIUnknownOptionException {
        try {
            cliCommandLine = new PosixParser().parse(cliOptions, argv);

            LongOptionProvider lp = null;
            ShortOptionProvider sp = null;

            final StringBuilder sb = new StringBuilder(16).append("--");

            for (@SuppressWarnings("unchecked") final Iterator<Option> iter = cliCommandLine.iterator(); iter.hasNext();) {
                final Option parsedOption = iter.next();
                final String parsedLongOpt = parsedOption.getLongOpt();

                final CLIOption opt = options.get(sb.append(parsedLongOpt).toString());
                if (null == opt) {
                    throw new CLIUnknownOptionException(parsedLongOpt);
                }
                sb.setLength(2);

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

            /*-
             * TODO: Enable this to allow unknown options
             * 
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
             */
        } catch (final UnrecognizedOptionException e) {
            throw new CLIUnknownOptionException(extractOption(e.getMessage()), e);
        } catch (final MissingArgumentException e) {
            final String optName = extractOption(e.getMessage());
            throw new CLIUnknownOptionException(
                optName,
                new StringBuilder(32).append("Missing argument for option '").append(optName).append('\'').toString(),
                e);
        } catch (final ParseException e) {
            throw new CLIParseException(argv, e);
        }
    }

    private static String extractOption(final String msg) {
        if (null == msg) {
            return null;
        }
        final int pos = msg.indexOf(':');
        if (pos >= 0) {
            return msg.substring(pos + 1);
        }
        return msg;
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
