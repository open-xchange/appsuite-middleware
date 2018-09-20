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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.logback.clt;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.ArrayUtils;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.logging.rmi.LogbackConfigurationRMIService;
import com.openexchange.logging.rmi.LogbackRemoteResponse;
import com.openexchange.logging.rmi.LogbackRemoteResponse.MessageType;
import ch.qos.logback.classic.Level;

/**
 * {@link LogbackConfigurationCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class LogbackConfigurationCLT extends AbstractLogbackConfigurationAdministrativeCLI<Void> {

    /**
     * Entry point
     * 
     * @param args The arguments of the command line tool
     */
    public static void main(String[] args) {
        new LogbackConfigurationCLT().execute(args);
    }

    private static final String validLogLevels = "{OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL}";
    private static final String SYNTAX = "logconf [[-a | -d] [-c <contextid> [-u <userid>] | -e <sessionid>] [-l <logger_name>=<logger_level> ...] -A <masterAdmin> -P <masterAdminPassword> [-p <RMI-Port>] [-s <RMI-Server]] | [-oec <category_1>,...] | [-cf] | [-lf] | [-ll [<logger_1> ...] | [dynamic]] | [-le] | [-h]";
    private static final String FOOTER = "\n\nThe flags -a and -d are mutually exclusive.\n\n\nValid log levels: " + validLogLevels + "\nValid categories: " + getValidCategories();

    /**
     * Initialises a new {@link LogbackConfigurationCLT}.
     */
    public LogbackConfigurationCLT() {
        super(SYNTAX, FOOTER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#addOptions(org.apache.commons.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        Option add = createSwitch("a", "add", "Flag to add the filter", true);
        Option del = createSwitch("d", "delete", "Flag to delete the filter", true);

        OptionGroup og = new OptionGroup();
        og.addOption(add).addOption(del);
        options.addOptionGroup(og);

        options.addOption(createArgumentOption("u", "user", "userId", "The user id for which to enable logging", false));
        options.addOption(createArgumentOption("c", "context", "contextId", "The context id for which to enable logging", false));
        options.addOption(createArgumentOption("oec", "override-exception-categories", "exceptionCategories", "Override the exception categories to be suppressed", false));
        options.addOption(createArgumentOption("e", "session", "sessionId", "The session id for which to enable logging", false));

        Option o = createOption("l", "level", false, "Define the log level (e.g. -l com.openexchange.appsuite=DEBUG). When the -d flag is present the arguments of this switch should be supplied without the level (e.g. -d -l com.openexchange.appsuite)", false);
        o.setArgs(Short.MAX_VALUE);
        options.addOption(o);

        options.addOption(createSwitch("ll", "list-loggers", "Get a list with all loggers of the system\nCan optionally have a list with loggers as arguments, i.e. -ll <logger1> <logger2> OR the keyword 'dynamic' that instructs the command line tool to fetch all dynamically modified loggers. Any other keyword is then ignored, and a full list will be retrieved.", false));
        options.addOption(createSwitch("lf", "list-filters", "Get a list with all logging filters of the system", false));
        options.addOption(createSwitch("cf", "clear-filters", "Clear all logging filters", false));
        options.addOption(createSwitch("le", "list-exception-category", "Get a list with all supressed exception categories", false));
        options.addOption(createSwitch("la", "list-appenders", "Lists all root appenders and any available statistics", false));

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        LogbackConfigurationRMIService logbackConfigService = getRmiStub(optRmiHostName, LogbackConfigurationRMIService.RMI_NAME);
        CommandLineExecutor executor = getCommandLineExecutor(options, cmd);
        executor.executeWith(cmd, logbackConfigService);
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#checkOptions(org.apache.commons.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        if (cmd.hasOption('u') && !cmd.hasOption('c')) {
            System.err.println("The '-u' should only be used in conjunction with the '-c' in order to specify a context.");
            printHelp();
        }

        if (cmd.hasOption('e') && (cmd.hasOption('u') || cmd.hasOption('c'))) {
            System.err.println("The '-e' and -u,-c options are mutually exclusive.");
            printHelp();
        }
    }

    /////////////////////////////////// HELPERS ///////////////////////////////////////

    /**
     * Return all valid OX Categories
     *
     * @return A string with a coma-separated-list of all valid {@link OXException} categories
     */
    private static String getValidCategories() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (Category.EnumCategory c : Category.EnumCategory.values()) {
            builder.append(c.toString()).append(", ");
        }
        builder.setCharAt(builder.length() - 2, '}');
        return builder.toString();
    }

    /**
     * Convert array to map
     *
     * @param loggersLevels
     * @return a {@link Map} with logger names and their respective logging {@link Level}
     */
    private static Map<String, Level> getLoggerMap(String[] loggersLevels) {
        if (loggersLevels == null) {
            return Collections.emptyMap();
        }

        Map<String, Level> levels = new HashMap<String, Level>();
        for (String s : loggersLevels) {
            String[] split = s.split("=");
            if (split.length != 2) {
                System.err.println("Warning: Ignoring unrecognized parameter for -l option '" + s + "'");
                continue;
            }
            if (isValidLogLevel(split[1])) {
                levels.put(split[0], Level.valueOf(split[1]));
            }
        }
        if (levels.isEmpty()) {
            throw new IllegalArgumentException("You must specify a key value pair for each logger along with its level, e.g. com.openexchange.appsuite=DEBUG");
        }
        return levels;
    }

    /**
     * Convert array to list
     *
     * @param loggersArray
     * @return A list with the logger names
     */
    private static List<String> getLoggerList(String[] loggersArray) {
        if (loggersArray == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(loggersArray);
    }

    /**
     * Validate whether the specified log level is in a recognized logback {@link Level}
     *
     * @param value loglevel
     * @return true/false
     */
    private static boolean isValidLogLevel(String value) {
        Level l = Level.toLevel(value, null);
        if (l != null) {
            return true;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Error: Unknown log level: \"").append(value).append("\".").append("Requires a valid log level: ").append(validLogLevels).append("\n");
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Verify whether the specified category is a valid OX Category
     *
     * @param category The name of the {@link OXException} category
     * @return <code>true</code> if the specified category is valid; <code>false</code> otherwise
     */
    private static boolean isValidCategory(String category) {
        if (category == null || category.equals("null")) {
            return false;
        }
        try {
            Category.EnumCategory.valueOf(Category.EnumCategory.class, category);
            return true;
        } catch (IllegalArgumentException e) {
            StringBuilder builder = new StringBuilder();
            builder.append("Error: Unknown category: \"").append(category).append("\".\"\n").append("Requires a valid category: ").append(getValidCategories()).append("\n");
            throw new IllegalArgumentException(builder.toString());
        }
    }

    private static void printResponse(LogbackRemoteResponse response) {
        if (response == null) {
            return;
        }
        for (MessageType t : MessageType.values()) {
            List<String> msgs = response.getMessages(t);
            if (!msgs.isEmpty()) {
                System.out.println(t.toString() + ": " + response.getMessages(t));
            }
        }
    }

    /**
     * Get the int value
     *
     * @param value The integer value as string
     * @return The integer value
     */
    private static final int getIntValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("You must specify an integer value.");
        }
    }

    /**
     * Prints the specified {@link Set}
     * 
     * @param set The {@link Set} to print
     */
    private static void printSet(Set<String> set) {
        Iterator<String> i = set.iterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }
    }

    /**
     * Retrieves the appropriate {@link CommandLineExecutor} according to the set {@link Options} in the {@link CommandLine} tool.
     * If no valid {@link CommandLineExecutor} is found then the usage of this tool is printed and the JVM is terminated.
     * 
     * @param options The available {@link Options} of the command line tool
     * @param cmd The {@link CommandLine}
     * @return The {@link CommandLineExecutor}
     */
    private CommandLineExecutor getCommandLineExecutor(Options options, CommandLine cmd) {
        try {
            if (cmd.hasOption('e')) {
                return CommandLineExecutor.SESSION;
            } else if (cmd.hasOption('c') && !cmd.hasOption('u')) {
                return CommandLineExecutor.CONTEXT;
            } else if (cmd.hasOption('u')) {
                return CommandLineExecutor.USER;
            } else if (cmd.hasOption('l')) {
                return CommandLineExecutor.MODIFY;
            } else if (cmd.hasOption("le")) {
                return CommandLineExecutor.LIST_CATEGORIES;
            } else if (cmd.hasOption("lf")) {
                return CommandLineExecutor.LIST_FILTERS;
            } else if (cmd.hasOption("ll")) {
                return CommandLineExecutor.LIST_LOGGERS;
            } else if (cmd.hasOption("oec")) {
                return CommandLineExecutor.OVERRIDE_EXCEPTION_CATEGORIES;
            } else if (cmd.hasOption("cf")) {
                return CommandLineExecutor.CLEAR_FILTERS;
            } else if (cmd.hasOption("la")) {
                return CommandLineExecutor.ROOT_APPENDER_STATS;
            } else {
                printHelp(options);
                System.exit(0);
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            printHelp(options);
            System.exit(1);
        }
        return null;
    }

    //////////////////////////////// NESTED /////////////////////////////////

    /**
     * {@link CommandLineExecutor} - Specifies all command line executors
     */
    private enum CommandLineExecutor {
        CONTEXT {

            @Override
            void executeWith(CommandLine commandLine, LogbackConfigurationRMIService logbackConfigService) throws RemoteException {
                int contextId = getIntValue(commandLine.getOptionValue("c"));
                LogbackRemoteResponse response = null;
                if (commandLine.hasOption('a')) {
                    response = logbackConfigService.filterContext(contextId, getLoggerMap(commandLine.getOptionValues('l')));
                } else if (commandLine.hasOption('d')) {
                    response = logbackConfigService.removeContextFilter(contextId, getLoggerList(commandLine.getOptionValues('l')));
                } else {
                    throw new IllegalArgumentException("You must specify a life cycle switch, either -a to add a filter or -d to remove a filter.");
                }
                printResponse(response);
            }
        },
        USER {

            @Override
            void executeWith(CommandLine commandLine, LogbackConfigurationRMIService logbackConfigService) throws RemoteException {
                int contextId = getIntValue(commandLine.getOptionValue("c"));
                int userId = getIntValue(commandLine.getOptionValue("u"));
                LogbackRemoteResponse response = null;
                if (commandLine.hasOption('a')) {
                    response = logbackConfigService.filterUser(contextId, userId, getLoggerMap(commandLine.getOptionValues('l')));
                } else if (commandLine.hasOption('d')) {
                    response = logbackConfigService.removeUserFilter(contextId, userId, getLoggerList(commandLine.getOptionValues('l')));
                } else {
                    throw new IllegalArgumentException("You must specify a life cycle switch, either -a to add a filter or -d to remove a filter.");
                }
                printResponse(response);
            }
        },
        SESSION {

            @Override
            void executeWith(CommandLine commandLine, LogbackConfigurationRMIService logbackConfigService) throws RemoteException {
                String sessionId = commandLine.getOptionValue('e');
                LogbackRemoteResponse response = null;
                if (commandLine.hasOption('a')) {
                    response = logbackConfigService.filterSession(sessionId, getLoggerMap(commandLine.getOptionValues('l')));
                } else if (commandLine.hasOption('d')) {
                    response = logbackConfigService.removeSessionFilter(sessionId, getLoggerList(commandLine.getOptionValues('l')));
                } else {
                    throw new IllegalArgumentException("You must specify a life cycle switch, either -a to add a filter or -d to remove a filter.");
                }
                printResponse(response);
            }
        },
        MODIFY {

            @Override
            void executeWith(CommandLine commandLine, LogbackConfigurationRMIService logbackConfigService) throws RemoteException {
                printResponse(logbackConfigService.modifyLogLevels(getLoggerMap(commandLine.getOptionValues('l'))));
            }
        },
        LIST_CATEGORIES {

            @Override
            void executeWith(CommandLine commandLine, LogbackConfigurationRMIService logbackConfigService) throws RemoteException {
                printSet(logbackConfigService.listExceptionCategories());
            }
        },
        LIST_FILTERS {

            @Override
            void executeWith(CommandLine commandLine, LogbackConfigurationRMIService logbackConfigService) throws RemoteException {
                printSet(logbackConfigService.listFilters());
            }
        },
        LIST_LOGGERS {

            @Override
            void executeWith(CommandLine commandLine, LogbackConfigurationRMIService logbackConfigService) throws RemoteException {
                String[] llargs = commandLine.getArgs();
                if (llargs.length > 1) {
                    printSet(logbackConfigService.getLevelForLoggers(commandLine.getArgs()));
                } else if (llargs.length == 1 && llargs[0].equals("dynamic")) {
                    printSet(logbackConfigService.listDynamicallyModifiedLoggers());
                } else {
                    printSet(logbackConfigService.listLoggers());
                }
            }
        },
        CLEAR_FILTERS {

            @Override
            void executeWith(CommandLine commandLine, LogbackConfigurationRMIService logbackConfigService) throws RemoteException {
                logbackConfigService.clearFilters();
            }

        },
        ROOT_APPENDER_STATS {

            @Override
            void executeWith(CommandLine commandLine, LogbackConfigurationRMIService logbackConfigService) throws RemoteException {
                System.out.println(logbackConfigService.getRootAppenderStats());
            }
        },
        OVERRIDE_EXCEPTION_CATEGORIES {

            @Override
            void executeWith(CommandLine commandLine, LogbackConfigurationRMIService logbackConfigService) throws RemoteException {
                String[] v = commandLine.getOptionValues("oec");
                String[] oeca = commandLine.getArgs();
                Object[] oneArrayToRuleThemAll = ArrayUtils.addAll(v, oeca);
                if (oneArrayToRuleThemAll.length <= 0) {
                    throw new IllegalArgumentException("You must specify at least one exception category.");
                }
                StringBuilder builder = new StringBuilder();
                for (Object o : oneArrayToRuleThemAll) {
                    if (!(o instanceof String)) {
                        throw new IllegalArgumentException("The specified category is not of type string '" + o + "'");
                    }
                    String s = ((String) o).toUpperCase();
                    if (isValidCategory(s)) {
                        builder.append(s).append(",");
                    }
                }
                logbackConfigService.overrideExceptionCategories(builder.subSequence(0, builder.length() - 1).toString());
            }
        };

        /**
         * Executes the remote method with the specified command line arguments
         * 
         * @param commandLine The {@link CommandLine} containing the arguments
         * @param logbackConfigService The {@link LogbackConfigurationRMIService}
         * @throws RemoteException if an error is occurred
         */
        abstract void executeWith(CommandLine commandLine, LogbackConfigurationRMIService logbackConfigService) throws RemoteException;
    }
}
