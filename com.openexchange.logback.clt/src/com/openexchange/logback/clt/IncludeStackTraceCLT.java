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

package com.openexchange.logback.clt;

import java.util.HashMap;
import java.util.Map;
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
        
        options.addOption("H", "host", true, "The optional JMX host (default:localhost)");
        options.addOption("p", "port", true, "The optional JMX port (default:9999)");
        options.addOption("l", "login", true, "The optional JMX login (if JMX has authentication enabled)");
        options.addOption("s", "password", true, "The optional JMX password (if JMX has authentication enabled)");
        options.addOption(new Option(null, "responsetimeout", true, "The optional response timeout in seconds when reading data from server (default: 0s; infinite)"));
        
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
            
            String host = "localhost";
            if (cl.hasOption('H')) {
                String tmp = cl.getOptionValue('H');
                if (null != tmp) {
                    host = tmp.trim();
                }
            }
            
            int port = 9999;
            if (cl.hasOption('p')) {
                final String val = cl.getOptionValue('p');
                if (null != val) {
                    try {
                        port = Integer.parseInt(val.trim());
                    } catch (final NumberFormatException e) {
                        System.err.println(new StringBuilder("Port parameter is not a number: ").append(val).toString());
                        printUsage(0);
                        System.exit(1);
                    }
                    if (port < 1 || port > 65535) {
                        System.err.println(new StringBuilder("Port parameter is out of range: ").append(val).append(
                            ". Valid range is from 1 to 65535.").toString());
                        printUsage(0);
                        System.exit(1);
                    }
                }
            }

            int responseTimeout = 0;
            if (cl.hasOption("responsetimeout")) {
                final String val = cl.getOptionValue('p');
                if (null != val) {
                    try {
                        responseTimeout = Integer.parseInt(val.trim());
                    } catch (final NumberFormatException e) {
                        System.err.println("responsetimeout parameter is not a number: " + val);
                        printUsage(0);
                        System.exit(1);
                    }
                }
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
            
            if (responseTimeout > 0) {
                /*
                 * The value of this property represents the length of time (in milliseconds) that the client-side Java RMI runtime will
                 * use as a socket read timeout on an established JRMP connection when reading response data for a remote method invocation.
                 * Therefore, this property can be used to impose a timeout on waiting for the results of remote invocations;
                 * if this timeout expires, the associated invocation will fail with a java.rmi.RemoteException.
                 *
                 * Setting this property should be done with due consideration, however, because it effectively places an upper bound on the
                 * allowed duration of any successful outgoing remote invocation. The maximum value is Integer.MAX_VALUE, and a value of
                 * zero indicates an infinite timeout. The default value is zero (no timeout).
                 */
                System.setProperty("sun.rmi.transport.tcp.responseTimeout", Integer.toString(responseTimeout * 1000));
            }
            
            String jmxLogin = null;
            if (cl.hasOption('l')) {
                jmxLogin = cl.getOptionValue('l');
            }
            String jmxPassword = null;
            if (cl.hasOption('s')) {
                jmxPassword = cl.getOptionValue('s');
            }
            
            final Map<String, Object> environment;
            if (jmxLogin == null || jmxPassword == null) {
                environment = null;
            } else {
                environment = new HashMap<String, Object>(1);
                String[] creds = new String[] { jmxLogin, jmxPassword };
                environment.put(JMXConnector.CREDENTIALS, creds);
            }

            // Invoke MBean
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+host+":"+port+"/server");
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, environment);
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
