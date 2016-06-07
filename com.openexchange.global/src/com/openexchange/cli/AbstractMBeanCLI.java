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
import javax.management.remote.JMXServiceURL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.java.Strings;

/**
 * {@link AbstractMBeanCLI} - The abstract helper class for MBean-connecting command-line tools.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public abstract class AbstractMBeanCLI<R> extends AbstractAdministrativeCLI<R, MBeanServerConnection> {

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
    @Override
    public R execute(final String[] args) {
        final Options options = newOptions();
        boolean error = true;
        try {
            // Option for help
            options.addOption("h", "help", false, "Prints a help text");

            // Option for JMX connect & authentication
            options.addOption("H", "host", true, "The optional JMX host (default:localhost)");
            options.addOption("p", "port", true, "The optional JMX port (default:9999)");
            options.addOption(new Option(null, "responsetimeout", true, "The optional response timeout in seconds when reading data from server (default: 0s; infinite)"));
            options.addOption("l", "login", true, "The optional JMX login (if JMX authentication is enabled)");
            options.addOption("s", "password", true, "The optional JMX password (if JMX authentication is enabled)");

            // Check if administrative permission is required
            final boolean requiresAdministrativePermission = requiresAdministrativePermission();
            if (requiresAdministrativePermission) {
                options.addOption("A", "adminuser", true, "Admin username");
                options.addOption("P", "adminpass", true, "Admin password");
            }

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

            // Check for JMX host
            final String host = cmd.getOptionValue('t', "localhost");

            // Check for JMX port
            final int port = parsePort('p', 9999, cmd, options);

            // Check for JMX login/password
            String jmxLogin = null;
            if (cmd.hasOption('l')) {
                jmxLogin = cmd.getOptionValue('l');
            }
            String jmxPassword = null;
            if (cmd.hasOption('s')) {
                jmxPassword = cmd.getOptionValue('s');
            }

            // Check for response timeout
            if (cmd.hasOption("responsetimeout")) {
                int responseTimeout = parseInt("responsetimeout", 0, cmd, options);
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
            }

            // Check other mandatory options
            checkOptions(cmd, options);

            // Build JMX environment
            final Map<String, Object> environment;
            if (jmxLogin == null || jmxPassword == null) {
                environment = null;
            } else {
                environment = new HashMap<String, Object>(1);
                String[] creds = new String[] { jmxLogin, jmxPassword };
                environment.put(JMXConnector.CREDENTIALS, creds);
            }

            // Invoke MBean
            final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/server");
            final JMXConnector jmxConnector = JMXConnectorFactory.connect(url, environment);

            R retval = null;
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                try {
                    if (requiresAdministrativePermission) {
                        AuthenticatorMBean authenticator = authenticatorMBean(mbsc);
                        if (isAuthEnabled(authenticator)) {
                            // Options for administrative authentication
                            String adminLogin = cmd.getOptionValue('A');
                            if (Strings.isEmpty(adminLogin)) {
                                System.out.println("You must provide administrative credentials to proceed.");
                                printHelp(options);
                                // Use correct exit code, see com.openexchange.admin.console.BasicCommandlineOptions.SYSEXIT_MISSING_OPTION
                                System.exit(104);
                                return null;
                            }

                            String adminPassword = cmd.getOptionValue('P');
                            if (Strings.isEmpty(adminPassword)) {
                                System.out.println("You must provide administrative credentials to proceed.");
                                printHelp(options);
                                // Use correct exit code, see com.openexchange.admin.console.BasicCommandlineOptions.SYSEXIT_MISSING_OPTION
                                System.exit(104);
                                return null;
                            }

                            administrativeAuth(adminLogin, adminPassword, cmd, authenticator);
                        }
                    }
                    retval = invoke(options, cmd, mbsc);
                } catch (final Exception e) {
                    final Throwable t = e.getCause();
                    throw new ExecutionFault(null == t ? e : t);
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
        } catch (final ExecutionFault e) {
            final Throwable t = e.getCause();
            final String message = t.getMessage();
            System.err.println(null == message ? "An error occurred." : message);
        } catch (final ParseException e) {
            System.err.println("Unable to parse command line: " + e.getMessage());
            printHelp(options);
        } catch (final MalformedURLException e) {
            System.err.println("URL to connect to server is invalid: " + e.getMessage());
        } catch (final IOException e) {
            System.err.println("Unable to communicate with the server: " + e.getMessage());
        } catch (final RuntimeException e) {
            String message = e.getMessage();
            String clazzName = e.getClass().getName();
            System.err.println("A runtime error occurred: " + (null == message ? clazzName : new StringBuilder(clazzName).append(": ").append(message).toString()));
        } catch (final Error e) {
            String message = e.getMessage();
            String clazzName = e.getClass().getName();
            System.err.println("A JVM problem occurred: " + (null == message ? clazzName : new StringBuilder(clazzName).append(": ").append(message).toString()));
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
     * Gets the {@link AuthenticatorMBean} instance.
     *
     * @param mbsc The MBean server connection
     * @return The {@link AuthenticatorMBean} instance
     * @throws MalformedObjectNameException If generating object name fails
     */
    protected AuthenticatorMBean authenticatorMBean(final MBeanServerConnection mbsc) throws MalformedObjectNameException {
        return getMBean(mbsc, AuthenticatorMBean.class, AuthenticatorMBean.DOMAIN);
    }

    /**
     * Checks if authentication is enabled.
     * <p>
     * By default property <code>"MASTER_AUTHENTICATION_DISABLED"</code> gets examined.
     *
     * @param authenticator The authenticator MBean
     * @throws MBeanException If operation fails
     */
    protected boolean isAuthEnabled(AuthenticatorMBean authenticator) throws MBeanException {
        return !authenticator.isMasterAuthenticationDisabled();
    }

    /**
     * Performs appropriate administrative authentication.
     * <p>
     * This method needs only to be implemented in case {@link #requiresAdministrativePermission()} is supposed to return <code>true</code>.
     *
     * @param login The administrator login
     * @param password The administrator password
     * @param cmd The command line providing options
     * @param authenticator The authenticator MBean
     * @throws MBeanException If authentication fails
     */
    protected abstract void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException;

    /**
     * Adds this command-line tool's options.
     * <p>
     * Note following options are reserved:
     * <ul>
     * <li>-h / --help
     * <li>-t / --host
     * <li>-p / --port
     * <li>-l / --login
     * <li>-s / --password
     * <li>-A / --adminuser
     * <li>-P / --adminpass
     * </ul>
     *
     * @param options The options
     */
    @Override
    protected abstract void addOptions(Options options);

    /**
     * Invokes the MBean's method.
     *
     * @param option The options
     * @param cmd The command line providing parameters/options
     * @param mbsc The MBean server connection
     * @return The return value
     * @throws Exception If invocation fails
     */
    @Override
    protected abstract R invoke(Options option, CommandLine cmd, MBeanServerConnection mbsc) throws Exception;

    /**
     * Gets the MBean instance.
     *
     * @param mbsc The MBean server connection
     * @param clazz The MBean class
     * @param domain The MBean's domain
     * @return The MBean instance
     * @throws MalformedObjectNameException If generating object name fails
     * @see #getObjectName(String, String)
     */
    protected static <MBean> MBean getMBean(final MBeanServerConnection mbsc, final Class<? extends MBean> clazz, final String domain) throws MalformedObjectNameException {
        return MBeanServerInvocationHandler.newProxyInstance(mbsc, getObjectName(clazz.getName(), domain), clazz, false);
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
