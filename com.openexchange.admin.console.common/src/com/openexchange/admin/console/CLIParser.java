/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

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

    private static final String OPT_START = "--";

    private final boolean posix;

    private String[] remainingArgs;

    private final Map<String, CLIOption> options;

    private final Map<String, List<Object>> values;

    private final Options cliOptions;

    /**
     * Initializes a new non-strict {@link CLIParser}.
     */
    public CLIParser() {
        this(false);
    }

    /**
     * Initializes a new {@link CLIParser}.
     *
     * @param posix <code>true</code> to strictly parse command line in POSIX notation; otherwise <code>false</code>
     */
    public CLIParser(final boolean posix) {
        super();
        this.posix = posix;
        cliOptions = posix ? new Options() : null;
        options = new HashMap<String, CLIOption>(16);
        values = new HashMap<String, List<Object>>(16);
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
        if (shortForm != null) {
            this.options.put(sb.append('-').append(shortForm).toString(), opt);
            sb.setLength(0);
        }
        final String longForm = opt.longForm();
        this.options.put(sb.append(OPT_START).append(longForm).toString(), opt);

        if (posix) {
            cliOptions.addOption(shortForm == null ? " " : shortForm, longForm, opt.wantsValue(), "");
        }

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
     * Gets given option's value.
     *
     * @param o The option whose value shall be returned
     * @return The option's value or <code>null</code> if not present
     */
    public Object getOptionValue(final CLIOption o) {
        return getOptionValue(o, null, false);
    }

    /**
     * Gets given option's value.
     *
     * @param o The option whose value shall be returned
     * @param def The default value to return if option is not present
     * @param remove <code>true</code> to remove option's value from parser's known values; otherwise <code>false</code>
     * @return The option's value or <tt>def</tt> if not present
     */
    public Object getOptionValue(final CLIOption o, final Object def, final boolean remove) {
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
     * Returns true if the CLIParser contains the specified option; false otherwise
     * 
     * @param o The option
     * @return Returns true if the CLIParser contains the specified option; false otherwise
     */
    public boolean hasOption(final CLIOption o) {
        return values.containsKey(o.longForm());
    }

    /**
     * Gets the parsed values of all occurrences of given option, or an empty collection if the option was not set.
     *
     * @param option The option
     * @return The parsed values of all occurrences of given option
     */
    public final Collection<Object> getOptionValues(final CLIOption option) {
        final List<Object> result = new ArrayList<Object>(2);

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
        this.options.remove(sb.append(OPT_START).append(option.longForm()).toString());
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

        @Override
        public String getValue() {
            return cliCommandLine.getOptionValue(c);
        }

        @Override
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

        @Override
        public String getValue() {
            return cliCommandLine.getOptionValue(s);
        }

        @Override
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
        if (!posix) {
            manualParse(argv, locale);
            return;
        }
        /*
         * Parse in strict POSIX notation
         */
        try {
            final CommandLine cliCommandLine = new PosixParser().parse(cliOptions, argv);

            LongOptionProvider lp = null;
            ShortOptionProvider sp = null;

            final StringBuilder sb = new StringBuilder(16).append(OPT_START);

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

    private final void manualParse(final String[] args, final Locale locale) throws CLIIllegalOptionValueException, CLIUnknownOptionException {
        final List<String> otherArgs = new ArrayList<String>(2);
        int position = 0;
        while (position < args.length) {
            final String argument = args[position];
            final int len = argument.length();
            if ((len > 0) && ('-' == argument.charAt(0))) {
                if (OPT_START.equals(argument)) {
                    /*
                     * End of options
                     */
                    position++;
                    break;
                }
                if (len > 1 && '-' == argument.charAt(1)) {
                    /*
                     * Distinguish between "--arg=value" and "--arg value"
                     */
                    final int pos = argument.indexOf('=');
                    if (-1 == pos) {
                        /*
                         * Deal with "--arg value"
                         */
                        position = lookUpOption(argument, null, args, locale, position);
                    } else {
                        /*
                         * Deal with "--arg=value"
                         */
                        position = lookUpOption(argument.substring(0, pos), argument.substring(pos + 1), args, locale, position);
                    }
                } else if (len > 2) {
                    /*
                     * Deal with "-xyz"
                     */
                    final StringBuilder sb = new StringBuilder(2).append('-');
                    for (int i = 1; i < len; i++) {
                        final char c = argument.charAt(i);
                        final CLIOption opt = this.options.get(sb.append(c).toString());
                        if (opt == null) {
                            throw new CLIUnknownSuboptionException(argument, c);
                        }
                        if (opt.wantsValue()) {
                            throw new CLIUnknownOptionException(
                                argument,
                                new StringBuilder(32).append("Missing argument for option '").append(argument).append('\'').toString());
                        }
                        sb.setLength(1);
                        addValue(opt, opt.parseValue(null, locale));
                    }
                    position++;
                } else {
                    /*
                     * Deal with "-arg value"
                     */
                    position = lookUpOption(argument, null, args, locale, position);
                }
            } else {
                otherArgs.add(argument);
                position++;
            }
        }
        for (; position < args.length; ++position) {
            otherArgs.add(args[position]);
        }

        remainingArgs = otherArgs.toArray(new String[otherArgs.size()]);
    }

    private int lookUpOption(final String argument, final String valueArg, final String[] args, final Locale locale, final int position) throws CLIIllegalOptionValueException, CLIUnknownOptionException {
        final CLIOption opt = options.get(argument);
        if (null == opt) {
            throw new CLIUnknownOptionException(argument);
        }
        int pos = position;
        final Object value;
        if (opt.wantsValue()) {
            String val = valueArg;
            if (null == val) {
                if (++pos < args.length) {
                    val = args[pos];
                }
            }
            value = opt.parseValue(val, locale);
        } else {
            value = opt.parseValue(null, locale);
        }
        addValue(opt, value);
        return ++pos;
    }

    private void addValue(final CLIOption opt, final Object value) {
        final String longForm = opt.longForm();

        List<Object> vals = values.get(longForm);
        if (null == vals) {
            vals = new ArrayList<Object>(2);
            values.put(longForm, vals);
            final String shortForm = opt.shortForm();
            if (null != shortForm) {
                values.put(shortForm, vals);
            }
        }

        vals.add(value);
    }
}
