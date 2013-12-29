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

package com.openexchange.cli;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import com.openexchange.auth.mbean.AuthenticatorMBean;


/**
 * {@link AbstractMBeanCLI} - The abstract helper class for MBean-connecting command-line tools.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public abstract class AbstractMBeanCLI<R> {

    /**
     * Initializes a new {@link AbstractMBeanCLI}.
     */
    protected AbstractMBeanCLI() {
        super();
    }

    /**
     * Executes the command-line tool.
     *
     * @param args The arguments
     * @return The return value
     */
    public R execute(final String[] args) {
        final Options options = new ReservedOptions();
        boolean error = true;
        try {
            // Option for help
            options.addOption("h", "help", false, "Prints a help text");

            // Option for JMX connect & authentication
            options.addOption("p", "port", true, "The optional JMX port (default:9999)");
            options.addOption("l", "login", true, "The optional JMX login (if JMX authentication is enabled)");
            options.addOption("s", "password", true, "The optional JMX password (if JMX authentication is enabled)");

            // Check if administrative permission is required
            final boolean requiresAdministrativePermission = requiresAdministrativePermission();
            if (requiresAdministrativePermission) {
                options.addOption("A", "adminuser", true, "Admin username. In case -a/--all is provided master administrator's user name is required; else the one for context administrator");
                options.addOption("P", "adminpass", true, "Admin password. In case -a/--all is provided master administrator's password is required; else the one for context administrator");
            }

            // Initialize command-line parser & parse arguments
            final CommandLineParser parser = new PosixParser();
            final CommandLine cmd = parser.parse(options, args);

            // Check for JMX port
            int port = 9999;
            if (cmd.hasOption('p')) {
                final String val = cmd.getOptionValue('p');
                if (null != val) {
                    try {
                        port = Integer.parseInt(val.trim());
                    } catch (final NumberFormatException e) {
                        System.err.println(new StringBuilder("Port parameter is not a number: ").append(val).toString());
                        printHelp(options);
                        System.exit(1);
                    }
                    if (port < 1 || port > 65535) {
                        System.err.println(new StringBuilder("Port parameter is out of range: ").append(val).append(
                            ". Valid range is from 1 to 65535.").toString());
                        printHelp(options);
                        System.exit(1);
                    }
                }
            }

            // Check for JMX login/password
            String jmxLogin = null;
            if (cmd.hasOption('l')) {
                jmxLogin = cmd.getOptionValue('l');
            }
            String jmxPassword = null;
            if (cmd.hasOption('s')) {
                jmxPassword = cmd.getOptionValue('s');
            }

            // Check other mandatory options
            checkOptions(cmd);

            // Build JMX environment
            final Map<String, Object> environment;
            if (jmxLogin == null || jmxPassword == null) {
                environment = null;
            } else {
                environment = new HashMap<String, Object>(1);
                environment.put(JMXConnectorServer.AUTHENTICATOR, new JMXAuthenticatorImpl(jmxLogin, jmxPassword));
            }

            // Authentication
            if (requiresAdministrativePermission && !cmd.hasOption('A')) {
                System.out.println("You must provide administrative credentials to proceed.");
                printHelp(options);
                System.exit(-1);
                return null;
            }
            if (requiresAdministrativePermission && !cmd.hasOption('P')) {
                System.out.println("You must provide administrative credentials to proceed.");
                printHelp(options);
                System.exit(-1);
                return null;
            }
            final String login = cmd.getOptionValue('A');
            final String password = cmd.getOptionValue('P');

            // Invoke MBean
            final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + port + "/server");
            final JMXConnector jmxConnector = JMXConnectorFactory.connect(url, environment);

            R retval = null;
            try {
                final MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                try {
                    if (requiresAdministrativePermission) {
                        final AuthenticatorMBean authenticator = authenticatorMBean(mbsc);
                        administrativeAuth(login, password, cmd, authenticator);
                    }
                    retval = invoke(cmd, mbsc);
                } catch (final Exception e) {
                    final String errMsg = e.getMessage();
                    System.out.println(errMsg == null ? "An error occurred." : errMsg);
                }
            } finally {
                try {
                    jmxConnector.close();
                } catch (final Exception e) {
                    // Ignore
                }
            }

            error = false;
            return retval;
        } catch (final ParseException e) {
            System.err.println("Unable to parse command line: " + e.getMessage());
            printHelp(options);
        } catch (final MalformedURLException e) {
            System.err.println("URL to connect to server is invalid: " + e.getMessage());
        } catch (final IOException e) {
            System.err.println("Unable to communicate with the server: " + e.getMessage());
        } finally {
            if (error) {
                System.exit(1);
            }
        }
        return null;
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
     * Gets the {@link AuthenticatorMBean} instance.
     *
     * @param mbsc The MBean server connection
     * @return The {@link AuthenticatorMBean} instance
     * @throws MalformedObjectNameException If generating object name fails
     */
    protected AuthenticatorMBean authenticatorMBean(final MBeanServerConnection mbsc) throws MalformedObjectNameException {
        return getMBean(mbsc, AuthenticatorMBean.class);
    }

    /**
     * Checks other mandatory options.
     *
     * @param cmd The command line
     */
    protected abstract void checkOptions(CommandLine cmd);

    /**
     * Signals if this command-line tool requires administrative permission.
     *
     * @return <code>true</code> for administrative permission; otherwise <code>false</code>
     */
    protected abstract boolean requiresAdministrativePermission();

    /**
     * Performs appropriate administrative authentication.
     *
     * @param login The administrator login
     * @param password The administrator password
     * @param cmd The command line providing options
     * @param authenticator The authenticator MBean
     * @throws MBeanException If authentication fails
     */
    protected abstract void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException;

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

    /**
     * Adds this command-line tool's options.
     * <p>
     * Note following options are reserved:
     * <ul>
     * <li>-h / --help
     * <li>-p / --port
     * <li>-l / --login
     * <li>-s / --password
     * <li>-A / --adminuser
     * <li>-P / --adminpass
     * </ul>
     *
     * @param options The options
     */
    protected abstract void addOptions(Options options);

    /**
     * Invokes the MBean's method.
     *
     * @param cmd The command line providing parameters/options
     * @param mbsc The MBean server connection
     * @return The return value
     */
    protected abstract R invoke(CommandLine cmd, MBeanServerConnection mbsc);

    /**
     * Gets the MBean instance.
     *
     * @param mbsc The MBean server connection
     * @param clazz The MBean class
     * @return The MBean instance
     * @throws MalformedObjectNameException If generating object name fails
     * @see #getObjectName(String, String)
     */
    protected static <MBean> MBean getMBean(final MBeanServerConnection mbsc, final Class<? extends MBean> clazz) throws MalformedObjectNameException {
        return MBeanServerInvocationHandler.newProxyInstance(mbsc, getObjectName(clazz.getName(), AuthenticatorMBean.DOMAIN), clazz, false);
    }

    /**
     * Creates an appropriate instance of {@link ObjectName} from specified class name and domain name.
     *
     * @param className The class name to use as object name
     * @param domain The domain name
     * @return An appropriate instance of {@link ObjectName}
     * @throws MalformedObjectNameException If instantiation of {@link ObjectName} fails
     */
    protected static ObjectName getObjectName(final String className, final String domain) throws MalformedObjectNameException {
        final int pos = className.lastIndexOf('.');
        return new ObjectName(domain, "name", pos == -1 ? className : className.substring(pos + 1));
    }
}
