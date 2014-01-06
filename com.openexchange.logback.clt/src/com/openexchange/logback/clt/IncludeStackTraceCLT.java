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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import com.openexchange.logging.mbean.LogbackConfigurationMBean;

/**
 * {@link IncludeStackTraceCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class IncludeStackTraceCLT {

    private static final Options options = new Options();
    static {
        Option enable = createOption("e", "enable", false, false, "Flag to enable to include stack traces in HTTP-API JSON responses", true);
        Option disbale = createOption("d", "disable", false, false, "Flag to disable to include stack traces in HTTP-API JSON responses", true);

        OptionGroup og = new OptionGroup();
        og.addOption(enable).addOption(disbale);

        options.addOption(createOption("u", "user", true, false, "The user identifier", true));
        options.addOption(createOption("c", "context", true, false, "The context identifier", true));
        options.addOption(createOption("h", "help", false, false, "Print usage of the command line tool", false));
        options.addOptionGroup(og);
    }

    /**
     * Create an {@link Option} with the {@link OptionBuilder}
     */
    @SuppressWarnings("static-access")
    private static final Option createOption(String shortName, String longName, boolean hasArgs, boolean hasOptArgs, String description, boolean mandatory) {
        return OptionBuilder.withLongOpt(longName).hasArg(hasArgs).withDescription(description).isRequired(mandatory).create(shortName);
    }

    /**
     * @param args
     * @throws ParseException
     */
    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();

        try {
            final CommandLine cl = parser.parse(options, args);

            if (cl.hasOption("h")) {
                printUsage(0);
                return;
            }

            if (!cl.hasOption("c")) {
                System.out.println("Missing option -c/--context");
                printUsage(-1);
                return;
            }
            if (!cl.hasOption("u")) {
                System.out.println("Missing option -u/--user");
                printUsage(-1);
                return;
            }
            if (!cl.hasOption("e") && !cl.hasOption("d")) {
                System.out.println("Missing option -e/--enable or -d/--disable");
                printUsage(-1);
                return;
            }

            final int contextID = getIntValue(cl.getOptionValue("c"));
            final int userID = getIntValue(cl.getOptionValue("u"));
            final boolean enable = cl.hasOption("e") ? true : false;

            // Invoke MBean
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();

                LogbackConfigurationMBean mbean = logbackConfigurationMBean(mbsc);
                mbean.includeStackTraceForUser(userID, contextID, enable);

                System.out.println("Including stack trace information successfully " + (enable ? "enabled" : "disabled") + " for user " + userID + " in context " + contextID);
            } finally {
                try {
                    jmxConnector.close();
                } catch (Exception e) {
                    // Ignore
                }
            }

            System.exit(0);
            return;

        } catch (final ParseException e) {
            System.out.println(e.getMessage());
            printUsage(-1);
        } catch (final Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            printUsage(-1);
        }
    }

    private static LogbackConfigurationMBean logbackConfigurationMBean(MBeanServerConnection mbsc) throws MalformedObjectNameException {
        ObjectName logbackConfObjName = new ObjectName(LogbackConfigurationMBean.DOMAIN, LogbackConfigurationMBean.KEY, LogbackConfigurationMBean.VALUE);
        return MBeanServerInvocationHandler.newProxyInstance(mbsc, logbackConfObjName, LogbackConfigurationMBean.class, false);
    }

    private static final int getIntValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.out.println("Error: Requires an integer value.\n");
            printUsage(-1);
        }
        return -1;
    }

    /**
     * Print usage
     *
     * @param exitCode
     */
    private static final void printUsage(int exitCode) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(120);
        hf.printHelp("includestacktrace [-e | -d] [-u <userid>] [-c <contextid>] [-h]", null, options, "\n\nThe flags -e and -d are mutually exclusive.");
        System.exit(exitCode);
    }

}
