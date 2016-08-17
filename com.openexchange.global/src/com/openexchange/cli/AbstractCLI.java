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

package com.openexchange.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;


/**
 * {@link AbstractCLI} - The basic super class for command-line tools.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractCLI<R, C> {

    /** The associated options */
    protected Options options;

    /**
     * Initializes a new {@link AbstractCLI}.
     */
    protected AbstractCLI() {
        super();
    }

    /**
     * Executes the command-line tool.
     *
     * @param args The arguments
     * @return The return value
     */
    public R execute(final String[] args) {
        Options options = newOptions();
        boolean error = true;
        try {
            // Option for help
            options.addOption("h", "help", false, "Prints a help text");

            // Add other options
            addOptions(options);

            // Initialize command-line parser & parse arguments
            final CommandLineParser parser = new PosixParser();
            final CommandLine cmd = parser.parse(options, args);

            // Check if help output is requested
            if (cmd.hasOption('h')) {
                printHelp(options);
                System.exit(0);
                return null;
            }

            // Check other mandatory options
            checkOptions(cmd, options);

            R retval = null;
            try {
                retval = invoke(options, cmd, null);
            } catch (Exception e) {
                Throwable t = e.getCause();
                throw new ExecutionFault(null == t ? e : t);
            }

            error = false;
            return retval;
        } catch (final ExecutionFault e) {
            final Throwable t = e.getCause();
            final String message = t.getMessage();
            System.err.println(null == message ? "An error occurred." : message);
        } catch (final ParseException e) {
            System.err.println("Unable to parse command line: " + e.getMessage());
            printHelp(options);
        } catch (final RuntimeException e) {
            String message = e.getMessage();
            String clazzName = e.getClass().getName();
            System.err.println("A runtime error occurred: " + (null == message ? clazzName : new StringBuilder(clazzName).append(": ").append(message).toString()));
        } catch (final Throwable t) {
            String message = t.getMessage();
            String clazzName = t.getClass().getName();
            System.err.println("A JVM problem occurred: " + (null == message ? clazzName : new StringBuilder(clazzName).append(": ").append(message).toString()));
        } finally {
            if (error) {
                System.exit(1);
            }
        }
        return null;
    }

    /**
     * Invokes the CLI method.
     *
     * @param option The options
     * @param cmd The command line providing parameters/options
     * @param context The execution context; always <code>null</code>
     * @return The return value
     * @throws Exception If invocation fails
     */
    protected abstract R invoke(Options option, CommandLine cmd, C context) throws Exception;

    /**
     * Creates an initially empty {@link ReservedOptions} instance.
     *
     * @return The new options
     */
    protected ReservedOptions newOptions() {
        ReservedOptions options = new ReservedOptions();
        this.options = options;
        return options;
    }

    /**
     * Adds this command-line tool's options.
     * <p>
     * Note following options are reserved:
     * <ul>
     * <li>-h / --help
     * </ul>
     *
     * @param options The options
     */
    protected abstract void addOptions(Options options);

    /**
     * Checks other mandatory options.
     *
     * @param cmd The command line
     * @param options The associated options
     */
    protected void checkOptions(CommandLine cmd, Options options) {
        checkOptions(cmd);
    }

    /**
     * Checks other mandatory options.
     *
     * @param cmd The command line
     * @param options The associated options
     */
    protected abstract void checkOptions(CommandLine cmd);

    /**
     * Prints the <code>--help</code> text.
     *
     * @param options The help output
     */
    protected void printHelp() {
        Options options = this.options;
        if (null != options) {
            printHelp(options);
        }
    }

    /**
     * Prints the <code>--help</code> text.
     *
     * @param options The help output
     */
    protected void printHelp(final Options options) {
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(HelpFormatter.DEFAULT_WIDTH, getName(), null, options, getFooter(), false);
    }

    /**
     * Gets the banner to display at the end of the help
     *
     * @return The banner to display at the end of the help
     */
    protected abstract String getFooter();

    /**
     * Gets the syntax for this application.
     *
     * @return The syntax for this application
     */
    protected abstract String getName();

    // -----------------------------------------------------------------------------------------------------------------------

    /**
     * Parses & validates the port value for given option.
     * <p>
     * Exits gracefully if port value is invalid.
     *
     * @param opt The option name
     * @param defaultValue The default value
     * @param cmd The command line
     * @param options The options
     * @return The port value
     */
    protected int parsePort(final char opt, final int defaultValue, final CommandLine cmd, final Options options) {
        int port = defaultValue;
        // Check option & parse if present
        final String sPort = cmd.getOptionValue(opt);
        if (null != sPort) {
            try {
                port = Integer.parseInt(sPort.trim());
            } catch (final NumberFormatException e) {
                System.err.println("Port parameter is not a number: " + sPort);
                printHelp(options);
                System.exit(1);
            }
        }
        if (port < 1 || port > 65535) {
            System.err.println("Port parameter is out of range: " + sPort + ". Valid range is from 1 to 65535.");
            printHelp(options);
            System.exit(1);
        }
        return port;
    }

    /**
     * Parses & validates the <code>int</code> value for given option.
     * <p>
     * Exits gracefully if <code>int</code> value is invalid.
     *
     * @param opt The option name
     * @param defaultValue The default value
     * @param cmd The command line
     * @param options The options
     * @return The <code>int</code> value
     */
    protected int parseInt(final char opt, final int defaultValue, final CommandLine cmd, final Options options) {
        int i = defaultValue;
        // Check option & parse if present
        final String sInt = cmd.getOptionValue(opt);
        if (null != sInt) {
            try {
                i = Integer.parseInt(sInt.trim());
            } catch (final NumberFormatException e) {
                System.err.println("Integer parameter is not a number: " + sInt);
                printHelp(options);
                System.exit(1);
            }
        }
        return i;
    }

    /**
     * Parses & validates the <code>int</code> value for given option.
     * <p>
     * Exits gracefully if <code>int</code> value is invalid.
     *
     * @param longOpt The long option name
     * @param defaultValue The default value
     * @param cmd The command line
     * @param options The options
     * @return The <code>int</code> value
     */
    protected int parseInt(final String longOpt, final int defaultValue, final CommandLine cmd, final Options options) {
        int i = defaultValue;
        // Check option & parse if present
        final String sInt = cmd.getOptionValue(longOpt);
        if (null != sInt) {
            try {
                i = Integer.parseInt(sInt.trim());
            } catch (final NumberFormatException e) {
                System.err.println("Integer parameter is not a number: " + sInt);
                printHelp(options);
                System.exit(1);
            }
        }
        return i;
    }

}
