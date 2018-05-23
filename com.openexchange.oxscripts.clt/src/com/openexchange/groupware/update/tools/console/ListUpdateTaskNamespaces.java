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

package com.openexchange.groupware.update.tools.console;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import com.openexchange.groupware.update.tools.Constants;

/**
 * {@link ListUpdateTaskNamespaces}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class ListUpdateTaskNamespaces {

    private static final Options options;
    static {
        options = new Options();
        options.addOption("h", "help", false, "Prints the help");

        options.addOption("H", "host", true, "The optional JMX host (default:localhost)");
        options.addOption("p", "port", true, "The optional JMX port (default:9999)");
        options.addOption("l", "login", true, "The optional JMX login (if JMX has authentication enabled)");
        options.addOption("s", "password", true, "The optional JMX password (if JMX has authentication enabled)");
        options.addOption(new Option(null, "responsetimeout", true, "The optional response timeout in seconds when reading data from server (default: 0s; infinite)"));
    }

    private static final String HEADER = "";
    //@formatter:off
    private static final String FOOTER = "This tools lists all namespaces for any update tasks and/or update task sets. The outcome of this tool can be used to " +
        " populate the property 'com.openexchange.groupware.update.excludedUpdateTasks'. Entries in that property will result in excluding all update tasks that are part " + 
        " of that particular namespace.";
    //@formatter:on

    /**
     * Prints the help screen
     */
    private static void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("listnamespaceawareupdatetasks", HEADER, options, FOOTER);
    }

    /**
     * Initialises a new {@link ListUpdateTaskNamespaces}.
     */
    private ListUpdateTaskNamespaces() {
        super();
    }

    /**
     * Entry point
     * 
     * @param args The command line arguments to pass
     */
    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        boolean error = true;
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption('h')) {
                printHelp();
                System.exit(0);
            }
            String host = "localhost";
            if (cmd.hasOption('H')) {
                String tmp = cmd.getOptionValue('H');
                if (null != tmp) {
                    host = tmp.trim();
                }
            }
            int port = 9999;
            if (cmd.hasOption('p')) {
                String val = cmd.getOptionValue('p');
                if (null != val) {
                    try {
                        port = Integer.parseInt(val.trim());
                    } catch (NumberFormatException e) {
                        System.err.println(new StringBuilder("Port parameter is not a number: ").append(val).toString());
                        printHelp();
                        System.exit(1);
                    }
                    if (port < 1 || port > 65535) {
                        System.err.println(new StringBuilder("Port parameter is out of range: ").append(val).append(". Valid range is from 1 to 65535.").toString());
                        printHelp();
                        System.exit(1);
                    }
                }
            }
            int responseTimeout = 0;
            if (cmd.hasOption("responsetimeout")) {
                String val = cmd.getOptionValue('p');
                if (null != val) {
                    try {
                        responseTimeout = Integer.parseInt(val.trim());
                    } catch (NumberFormatException e) {
                        System.err.println("responsetimeout parameter is not a number: " + val);
                        printHelp();
                        System.exit(1);
                    }
                }
            }
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
            if (cmd.hasOption('l')) {
                jmxLogin = cmd.getOptionValue('l');
            }
            String jmxPassword = null;
            if (cmd.hasOption('s')) {
                jmxPassword = cmd.getOptionValue('s');
            }

            Map<String, Object> environment;
            if (jmxLogin == null || jmxPassword == null) {
                environment = null;
            } else {
                environment = new HashMap<String, Object>(1);
                String[] creds = new String[] { jmxLogin, jmxPassword };
                environment.put(JMXConnector.CREDENTIALS, creds);
            }

            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/server");
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, environment);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                Object ret = mbsc.invoke(Constants.OBJECT_NAME, "getNamespaceAware", new Object[] {}, null);
                if (ret != null && ret instanceof Map) {
                    Map<String, Set<String>> map = (Map<String, Set<String>>) ret;
                    for (Entry<String, Set<String>> entry : map.entrySet()) {
                        System.out.println("+- " + entry.getKey());
                        for (String c : entry.getValue()) {
                            System.out.println("|--- " + c);
                        }
                    }
                } else {
                    System.out.println("No namespace-aware update tasks found");
                }
            } finally {
                if (null != jmxConnector) {
                    try {
                        jmxConnector.close();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
            error = false;
        } catch (ParseException e) {
            System.err.println("Unable to parse command line: " + e.getMessage());
            printHelp();
        } catch (MalformedURLException e) {
            System.err.println("URL to connect to server is invalid: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Unable to communicate with the server: " + e.getMessage());
        } catch (ReflectionException e) {
            System.err.println("Problem with reflective type handling: " + e.getMessage());
        } catch (MBeanException e) {
            Throwable t = e.getCause();
            String message = null == t ? e.getMessage() : t.getMessage();
            System.err.println(null == message ? "Unexpected error." : "Unexpected error: " + message);
        } catch (InstanceNotFoundException e) {
            System.err.println("Instance is not available: " + e.getMessage());
        } finally {
            if (error) {
                System.exit(1);
            }
        }
    }
}
