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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import com.openexchange.ajax.requesthandler.converters.preview.cache.PreviewCacheMBean;

/**
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PreviewCacheTool {

    private static final Options sOptions;
    static {
        sOptions = new Options();
        sOptions.addOption("h", "help", false, "Prints a help text");
        sOptions.addOption("c", "context", true, "Required. The context identifier");
    }

    /**
     * Prevent instantiation.
     */
    private PreviewCacheTool() {
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

            if (!cmd.hasOption('c')) {
                System.out.println("Parameter 'context' is required.");
                printHelp();
                System.exit(1);
                return;
            }

            final String contextOptionVal = cmd.getOptionValue('c');

            // Invoke MBean
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();

                try {
                    PreviewCacheMBean previceCacheProxy = previewCacheMBean(mbsc);
                    previceCacheProxy.clearFor(Integer.parseInt(contextOptionVal.trim()));
                    System.out.println("All cache entries cleared for context " + contextOptionVal);
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
        helpFormatter.printHelp("clearpreviewcache", sOptions);
    }

    private static PreviewCacheMBean previewCacheMBean(MBeanServerConnection mbsc) throws MalformedObjectNameException {
        return MBeanServerInvocationHandler.newProxyInstance(mbsc, getObjectName(PreviewCacheMBean.class.getName(), "com.openexchange.preview.cache"), PreviewCacheMBean.class, false);
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
