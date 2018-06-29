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
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.ArrayUtils;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.exception.Category;
import com.openexchange.logging.rmi.LogbackConfigurationRMIService;
import com.openexchange.logging.rmi.LogbackRemoteResponse;
import com.openexchange.logging.rmi.LogbackRemoteResponse.MessageType;
import ch.qos.logback.classic.Level;

/**
 * {@link LogbackConfigurationCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class LogbackConfigurationCLT extends AbstractRmiCLI<Void> {

    /**
     * Entry point
     * 
     * @param args The arguments of the command line tool
     */
    public static void main(String[] args) {
        new LogbackConfigurationCLT().execute(args);
    }

    private static final String validLogLevels = "{OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL}";
    private static final String SYNTAX = "logconf [[-a | -d] [-c <contextid> [-u <userid>] | -s <sessionid>] [-l <logger_name>=<logger_level> ...] [-U <JMX-User> -P <JMX-Password> [-p <JMX-Port>]]] | [-oec <category_1>,...] | [-cf] | [-lf] | [-ll [<logger_1> ...] | [dynamic]] | [-le] | [-h]";
    private static final String FOOTER = "\n\nThe flags -a and -d are mutually exclusive.\n\n\nValid log levels: " + validLogLevels + "\nValid categories: " + getValidCategories();

    /**
     * Initialises a new {@link LogbackConfigurationCLT}.
     */
    public LogbackConfigurationCLT() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#administrativeAuth(java.lang.String, java.lang.String, org.apache.commons.cli.CommandLine, com.openexchange.auth.rmi.RemoteAuthenticator)
     */
    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        try {
            authenticator.doAuthentication(login, password);
        } catch (RemoteException e) {
            System.err.print(e.getMessage());
            System.exit(-1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#addOptions(org.apache.commons.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        Option add = createOption("a", "add", false, "Flag to add the filter", true);
        Option del = createOption("d", "delete", false, "Flag to delete the filter", true);

        OptionGroup og = new OptionGroup();
        og.addOption(add).addOption(del);
        options.addOptionGroup(og);

        options.addOption(createOption("u", "user", true, "The user id for which to enable logging", false));
        options.addOption(createOption("c", "context", true, "The context id for which to enable logging", false));
        options.addOption(createOption("oec", "override-exception-categories", true, "Override the exception categories to be suppressed", false));
        options.addOption(createOption("s", "session", true, "The session id for which to enable logging", false));

        Option o = createOption("l", "level", false, "Define the log level (e.g. -l com.openexchange.appsuite=DEBUG). When the -d flag is present the arguments of this switch should be supplied without the level (e.g. -d -l com.openexchange.appsuite)", false);
        o.setArgs(Short.MAX_VALUE);
        options.addOption(o);

        options.addOption(createOption("ll", "list-loggers", false, "Get a list with all loggers of the system\nCan optionally have a list with loggers as arguments, i.e. -ll <logger1> <logger2> OR the keyword 'dynamic' that instructs the command line tool to fetch all dynamically modified loggers. Any other keyword is then ignored, and a full list will be retrieved.", false));
        options.addOption(createOption("lf", "list-filters", false, "Get a list with all logging filters of the system", false));
        options.addOption(createOption("cf", "clear-filters", false, "Clear all logging filters", false));
        options.addOption(createOption("le", "list-exception-category", false, "Get a list with all supressed exception categories", false));
        options.addOption(createOption("la", "list-appenders", false, "Lists all root appenders and any available statistics", false));

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        LogbackConfigurationRMIService logbackConfigService = getRmiStub(optRmiHostName, LogbackConfigurationRMIService.RMI_NAME);
        if (cmd.hasOption('s')) {
            CommandLineExecutor.SESSION.executeWith(cmd, logbackConfigService);
        } else if (cmd.hasOption('c') && !cmd.hasOption('u')) {
            CommandLineExecutor.CONTEXT.executeWith(cmd, logbackConfigService);
        } else if (cmd.hasOption('u')) {
            CommandLineExecutor.USER.executeWith(cmd, logbackConfigService);
        } else if (cmd.hasOption('l')) {
            CommandLineExecutor.MODIFY.executeWith(cmd, logbackConfigService);
        } else if (cmd.hasOption("le")) {
            CommandLineExecutor.LIST_CATEGORIES.executeWith(cmd, logbackConfigService);
        } else if (cmd.hasOption("lf")) {
            CommandLineExecutor.LIST_FILTERS.executeWith(cmd, logbackConfigService);
        } else if (cmd.hasOption("ll")) {
            CommandLineExecutor.LIST_LOGGERS.executeWith(cmd, logbackConfigService);
        } else if (cmd.hasOption("oec")) {
            CommandLineExecutor.OVERRIDE_EXCEPTION_CATEGORIES.executeWith(cmd, logbackConfigService);
        } else if (cmd.hasOption("cf")) {
            CommandLineExecutor.CLEAR_FILTERS.executeWith(cmd, logbackConfigService);
        } else if (cmd.hasOption("la")) {
            CommandLineExecutor.ROOT_APPENDER_STATS.executeWith(cmd, logbackConfigService);
        } else {
            printHelp();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractAdministrativeCLI#requiresAdministrativePermission()
     */
    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
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

        if (cmd.hasOption('s') && (cmd.hasOption('u') || cmd.hasOption('c'))) {
            System.err.println("The '-s' and -u,-c options are mutually exclusive.");
            printHelp();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getFooter()
     */
    @Override
    protected String getFooter() {
        return FOOTER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getName()
     */
    @Override
    protected String getName() {
        return SYNTAX;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#printHelp()
     */
    @Override
    protected void printHelp() {
        printHelp(options);
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.cli.AbstractCLI#printHelp(org.apache.commons.cli.Options)
     */
    @Override
    protected void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(120, getName(), getHeader(), options, getFooter(), false);
    }

    /////////////////////////////////// HELPERS ///////////////////////////////////////

    /**
     * Create an {@link Option} with the {@link OptionBuilder}
     *
     * @param shortName short name of the option
     * @param longName long name of the option
     * @param hasArgs whether it has arguments
     * @param description short description
     * @param mandatory whether it is mandatory
     * @return The {@link Option}
     */
    private final Option createOption(String shortName, String longName, boolean hasArgs, String description, boolean mandatory) {
        OptionBuilder.withLongOpt(longName);
        OptionBuilder.hasArg(hasArgs);
        OptionBuilder.withDescription(description);
        OptionBuilder.isRequired(mandatory);
        return OptionBuilder.create(shortName);
    }

    /**
     * Return all valid OX Categories
     *
     * @return
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
     * @return
     */
    private static Map<String, Level> getLoggerMap(String[] loggersLevels) {
        if (loggersLevels == null) {
            return Collections.emptyMap();
        }

        Map<String, Level> levels = new HashMap<String, Level>();
        for (String s : loggersLevels) {
            String[] split = s.split("=");
            if (split.length != 2) {
                System.err.println("Warning: Ignoring unrecognized parameter for -l option");
                continue;
            }
            if (isValidLogLevel(split[1])) {
                levels.put(split[0], Level.valueOf(split[1]));
            }
        }
        return levels;
    }

    /**
     * Convert array to list
     *
     * @param loggersArray
     * @return
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

        //printUsage(-1);
        return false;
    }

    /**
     * Verify whether the specified category is a valid OX Category
     *
     * @param category
     * @return
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
            System.out.println(builder.toString());
            //printHelp();
            return false;
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
     * @param value
     * @return
     */
    private static final int getIntValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.out.println("Error: Requires an integer value.\n");
            //printUsage(-1);
        }
        return -1;
    }

    private static void printSet(Set<String> set) {
        Iterator<String> i = set.iterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }
    }

    /**
     * Print usage
     *
     * @param exitCode
     */
    //    private static final void printUsage(int exitCode) {
    //        HelpFormatter hf = new HelpFormatter();
    //        hf.setWidth(120);
    //        hf.printHelp("logconf [[-a | -d] [-c <contextid> [-u <userid>] | -s <sessionid>] [-l <logger_name>=<logger_level> ...] [-U <JMX-User> -P <JMX-Password> [-p <JMX-Port>]]] | [-oec <category_1>,...] | [-cf] | [-lf] | [-ll [<logger_1> ...] | [dynamic]] | [-le] | [-h]", null, options, "\n\nThe flags -a and -d are mutually exclusive.\n\n\nValid log levels: " + validLogLevels + "\nValid categories: " + getValidCategories());
    //        System.exit(exitCode);
    //    }

    //////////////////////////////// NESTED /////////////////////////////////

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
                }
                printResponse(response);
            }
        },
        SESSION {

            @Override
            void executeWith(CommandLine commandLine, LogbackConfigurationRMIService logbackConfigService) throws RemoteException {
                String sessionId = commandLine.getOptionValue('s');
                LogbackRemoteResponse response = null;
                if (commandLine.hasOption('a')) {
                    response = logbackConfigService.filterSession(sessionId, getLoggerMap(commandLine.getOptionValues('l')));
                } else if (commandLine.hasOption('d')) {
                    response = logbackConfigService.removeSessionFilter(sessionId, getLoggerList(commandLine.getOptionValues('l')));
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
                    //printUsage(-1);
                    return;
                }
                StringBuilder builder = new StringBuilder();
                for (Object o : oneArrayToRuleThemAll) {
                    if (!(o instanceof String)) {
                        //printUsage(-1);
                        return;
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
