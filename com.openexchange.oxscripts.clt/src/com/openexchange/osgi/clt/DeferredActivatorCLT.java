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

package com.openexchange.osgi.clt;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
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
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.osgi.mbean.DeferredActivatorMBean;
import com.openexchange.osgi.mbean.DeferredActivatorMBeanImpl;

/**
 * {@link DeferredActivatorCLT}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DeferredActivatorCLT {

    /**
     * Initializes a new {@link DeferredActivatorCLT}.
     */
    public DeferredActivatorCLT() {
        super();
    }

    public static void main(final String[] args) {
        final CommandLineParser parser = new PosixParser();
        String bundleName = null;
        boolean testAll = true;
        try {
            final CommandLine cmd = parser.parse(toolkitOptions, args);
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
                final String val = cmd.getOptionValue('p');
                if (null != val) {
                    try {
                        port = Integer.parseInt(val.trim());
                    } catch (final NumberFormatException e) {
                        System.err.println(new StringBuilder("Port parameter is not a number: ").append(val).toString());
                        printHelp();
                        System.exit(3);
                    }
                    if (port < 1 || port > 65535) {
                        System.err.println(new StringBuilder("Port parameter is out of range: ").append(val).append(
                            ". Valid range is from 1 to 65535.").toString());
                        printHelp();
                        System.exit(3);
                    }
                }
            }

            int responseTimeout = 0;
            if (cmd.hasOption("responsetimeout")) {
                final String val = cmd.getOptionValue('p');
                if (null != val) {
                    try {
                        responseTimeout = Integer.parseInt(val.trim());
                    } catch (final NumberFormatException e) {
                        System.err.println("responsetimeout parameter is not a number: " + val);
                        printHelp();
                        System.exit(1);
                    }
                }
            }

            if (cmd.hasOption('n')) {
                bundleName = cmd.getOptionValue('n');
                testAll = false;
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

            final Map<String, Object> environment;
            if (jmxLogin == null || jmxPassword == null) {
                environment = null;
            } else {
                environment = new HashMap<String, Object>(1);
                String[] creds = new String[] { jmxLogin, jmxPassword };
                environment.put(JMXConnector.CREDENTIALS, creds);
            }

            final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/server");
            final JMXConnector jmxConnector = JMXConnectorFactory.connect(url, environment);
            try {
                final MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();

                if (testAll) {
                    final Object result = mbsc.invoke(
                        getObjectName(DeferredActivatorMBeanImpl.class.getName(), DeferredActivatorMBean.OSGI_DOMAIN),
                        "listAllMissingServices",
                        null,
                        null);
                    if (result instanceof Map<?, ?>) {
                        if (((Map) result).isEmpty()) {
                            System.out.println("No services missing");
                            System.exit(0);
                        }
                        for (Object bundle : ((Map) result).keySet()) {
                            Object list = ((Map) result).get(bundle);
                            if (list instanceof List<?>) {
                                System.out.println("Services missing for bundle " + bundle.toString() + ":");
                                final StringBuilder sb = new StringBuilder();
                                for (final Object o : (List) list) {
                                    sb.append(o.toString()).append(',');
                                }
                                sb.deleteCharAt(sb.length() - 1);
                                System.out.println(sb.toString());
                            }
                            System.out.println();
                        }
                        System.exit(2);
                    }
                } else {
                    final String[] signature = new String[] { String.class.getName() };
                    final Object[] params = new Object[] { bundleName };
                    final Object result = mbsc.invoke(
                        getObjectName(DeferredActivatorMBeanImpl.class.getName(), DeferredActivatorMBean.OSGI_DOMAIN),
                        "listMissingServices",
                        params,
                        signature);
                    if (result instanceof ArrayList<?>) {
                        final ArrayList<?> missing = (ArrayList<?>) result;
                        if (missing.isEmpty()) {
                            System.out.println("No services missing for bundle " + bundleName);
                            System.exit(0);
                        } else {
                            System.out.println("Services missing for bundle " + bundleName + ":");
                            final StringBuilder sb = new StringBuilder();
                            for (final Object o : missing) {
                                sb.append(o.toString()).append(',');
                            }
                            sb.deleteCharAt(sb.length() - 1);
                            System.out.println(sb.toString());
                            System.exit(2);
                        }
                    }
                }
            } finally {
                Streams.close(jmxConnector);
            }
        } catch (final MalformedObjectNameException e) {
            // Cannot occur
            System.err.println("Invalid MBean name: " + e.getMessage());
            System.exit(3);
        } catch (final ParseException e) {
            System.err.println("Unable to parse command line: " + e.getMessage());
            printHelp();
        } catch (final MalformedURLException e) {
            System.err.println("URL to connect to server is invalid: " + e.getMessage());
            System.exit(3);
        } catch (final IOException e) {
            System.err.println("Unable to communicate with the server: " + e.getMessage());
            System.exit(3);
        } catch (final InstanceNotFoundException e) {
            System.err.println("Instance is not available: " + e.getMessage());
            System.exit(3);
        } catch (final MBeanException e) {
            final Throwable t = e.getCause();
            final String message;
            if (null == t) {
                message = e.getMessage();
            } else {
                if ((t instanceof OXException)) {
                    final OXException oxe = (OXException) t;
                    if ("CTX".equals(oxe.getPrefix())) {
                        message = "Cannot find bundle " + bundleName;
                    } else {
                        message = t.getMessage();
                    }
                } else {
                    message = t.getMessage();
                }
            }
            System.err.println(null == message ? "Unexpected error." : "Unexpected error: " + message);
            System.exit(3);
        } catch (final ReflectionException e) {
            System.err.println("Problem with reflective type handling: " + e.getMessage());
            System.exit(3);
        } catch (final RuntimeException e) {
            System.err.println("Problem in runtime: " + e.getMessage());
            printHelp();
            System.exit(3);
        }
    }

    /**
     * Creates an appropriate instance of {@link ObjectName} from specified class name and domain name.
     *
     * @param className The class name to use as object name
     * @param domain The domain name
     * @return An appropriate instance of {@link ObjectName}
     * @throws MalformedObjectNameException If instantiation of {@link ObjectName} fails
     */
    private static ObjectName getObjectName(final String className, final String domain) throws MalformedObjectNameException {
        final int pos = className.lastIndexOf('.');
        return new ObjectName(domain, "name", pos == -1 ? className : className.substring(pos + 1));
    }

    private static final Options toolkitOptions;

    static {
        toolkitOptions = new Options();
        toolkitOptions.addOption("h", "help", false, "Prints a help text");

        toolkitOptions.addOption("n", "name", true, "The optional bundle's symbolic name");

        toolkitOptions.addOption("H", "host", true, "The optional JMX host (default:localhost)");
        toolkitOptions.addOption("p", "port", true, "The optional JMX port (default:9999)");
        toolkitOptions.addOption("l", "login", true, "The optional JMX login (if JMX has authentication enabled)");
        toolkitOptions.addOption("s", "password", true, "The optional JMX password (if JMX has authentication enabled)");
        toolkitOptions.addOption(new Option(null, "responsetimeout", true, "The optional response timeout in seconds when reading data from server (default: 0s; infinite)"));
    }

    private static void printHelp() {
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("servicestate", toolkitOptions);
    }

}
