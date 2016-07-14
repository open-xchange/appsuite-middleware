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

package com.openexchange.ajax.requesthandler.converters.preview.cache.console;

import java.io.IOException;
import java.net.MalformedURLException;
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
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import com.openexchange.ajax.requesthandler.converters.preview.cache.ResourceCacheMBean;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PreviewCacheTool2 {

    private static final Options sOptions;
    static {
        sOptions = new Options();
        sOptions.addOption("h", "help", false, "Prints a help text");
        sOptions.addOption("c", "context", true, "Required. The context identifier");
        sOptions.addOption("a", "all", false, "Required. The flag to signal that contexts shall be processed. Hence option -c/--context is then obsolete.");
        sOptions.addOption("i", "invalids", true, "An optional comma-separated list of those MIME types that should be considered as broken/corrupt. Default is \"application/force-download, application/x-download, application/$suffix\"");

        sOptions.addOption("H", "host", true, "The optional JMX host (default:localhost)");
        sOptions.addOption("p", "port", true, "The optional JMX port (default:9999)");
        sOptions.addOption("l", "login", true, "The optional JMX login (if JMX has authentication enabled)");
        sOptions.addOption("s", "password", true, "The optional JMX password (if JMX has authentication enabled)");
    }

    /**
     * Prevent instantiation.
     */
    private PreviewCacheTool2() {
        super();
    }

    /**
     * Main method for starting from console.
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        boolean error = true;
        try {
            final CommandLine cmd = parser.parse(sOptions, args);
            if (cmd.hasOption('h')) {
                printHelp();
                System.exit(0);
                return;
            }

            final String contextOptionVal;
            if (cmd.hasOption('a')) {
                contextOptionVal = null;
            } else {
                if (!cmd.hasOption('c')) {
                    System.out.println("Either parameter 'context' or parameter 'all' is required.");
                    printHelp();
                    System.exit(1);
                    return;
                }
                contextOptionVal = cmd.getOptionValue('c');
            }

            String invalids = null;
            if (cmd.hasOption('i')) {
                invalids = cmd.getOptionValue('i');
                invalids = invalids.trim();
                if (invalids.startsWith("\"") && invalids.endsWith("\"")) {
                    invalids = invalids.substring(1, invalids.length() - 1);
                    invalids = invalids.trim();
                }
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
                        System.exit(1);
                    }
                    if (port < 1 || port > 65535) {
                        System.err.println(new StringBuilder("Port parameter is out of range: ").append(val).append(
                            ". Valid range is from 1 to 65535.").toString());
                        printHelp();
                        System.exit(1);
                    }
                }
            }

            String jmxLogin = null;
            if (cmd.hasOption('l')) {
                jmxLogin = cmd.getOptionValue('l');
            }
            String jmxPassword = null;
            if (cmd.hasOption('s')) {
                jmxPassword = cmd.getOptionValue('s');
            }

            // Environment
            final Map<String, Object> environment;
            if (jmxLogin == null || jmxPassword == null) {
                environment = null;
            } else {
                environment = new HashMap<String, Object>(1);
                String[] creds = new String[] { jmxLogin, jmxPassword };
                environment.put(JMXConnector.CREDENTIALS, creds);
            }

            // Invoke MBean
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/server");
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, environment);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();

                try {
                    ResourceCacheMBean previceCacheProxy = previewCacheMBean(mbsc);
                    String resultDesc = previceCacheProxy.sanitizeMimeTypesInDatabaseFor(null == contextOptionVal ? -1 : Integer.parseInt(contextOptionVal.trim()), invalids);
                    System.out.println(resultDesc);
                } catch (Exception e) {
                    String errMsg = e.getMessage();
                    System.out.println(errMsg == null ? "An error occurred." : errMsg);
                }
            } finally {
                try {
                    jmxConnector.close();
                } catch (Exception e) {
                    // Ignore
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
        } finally {
            if (error) {
                System.exit(1);
            }
        }
    }

    private static void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(HelpFormatter.DEFAULT_WIDTH, "sanitizefilemimetypes", null, sOptions, "The options -c/--context and -a/--all are mutually exclusive.", false);
    }

    private static ResourceCacheMBean previewCacheMBean(MBeanServerConnection mbsc) throws MalformedObjectNameException {
        return MBeanServerInvocationHandler.newProxyInstance(mbsc, getObjectName(ResourceCacheMBean.class.getName(), "com.openexchange.preview.cache"), ResourceCacheMBean.class, false);
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

}
