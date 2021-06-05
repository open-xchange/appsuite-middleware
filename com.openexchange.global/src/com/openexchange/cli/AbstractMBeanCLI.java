/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.openexchange.auth.mbean.AuthenticatorMBean;

/**
 * {@link AbstractMBeanCLI} - The abstract helper class for MBean-connecting command-line tools.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public abstract class AbstractMBeanCLI<R> extends AbstractAdministrativeCLI<R, MBeanServerConnection, AuthenticatorMBean> {

    private MBeanServerConnection executionContext;

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
    public R execute(String[] args) {
        Options options = newOptions();
        boolean error = true;
        try {
            // Option for help
            options.addOption(createSwitch("h", "help", "Prints this help text", false));

            // Option for JMX connect & authentication
            options.addOption(createArgumentOption("H", "host", "jmxHost", "The optional JMX host (default:localhost)", false));
            options.addOption(createArgumentOption("p", "port", "jmxPort", "The optional JMX port (default:9999)", false));
            options.addOption(createArgumentOption(null, "responsetimeout", "timeout", "The optional response timeout in seconds when reading data from server (default: 0s; infinite)", false));
            options.addOption(createArgumentOption("l", "login", "jmxLogin", "The optional JMX login (if JMX authentication is enabled)", false));
            options.addOption(createArgumentOption("s", "password", "jmxPassword", "The optional JMX password (if JMX authentication is enabled)", false));

            // Check if administrative permission is required and add the admin options if necessary
            boolean requiresAdministrativePermission = optAdministrativeOptions(args);

            // Add other options
            addOptions(options);

            // Check if help output is requested
            if (helpRequested(args)) {
                printHelp(options);
                System.exit(0);
                return null;
            }

            // Initialize command-line parser & parse arguments
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            // Check for JMX host
            String host = cmd.getOptionValue('H', "localhost");

            // Check for JMX port
            int port = parsePort('p', 9999, cmd, options);

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
            Map<String, Object> environment;
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

            R retval = null;
            try {
                executionContext = jmxConnector.getMBeanServerConnection();
                try {
                    if (requiresAdministrativePermission) {
                        optAuthenticate(cmd);
                    }
                    retval = invoke(options, cmd, executionContext);
                } catch (Exception e) {
                    Throwable t = e.getCause();
                    throw new ExecutionFault(null == t ? e : t);
                }
            } finally {
                try {
                    jmxConnector.close();
                } catch (Exception e) {
                    // Ignore
                }
            }

            error = false;
            return retval;
        } catch (ExecutionFault e) {
            Throwable t = e.getCause();
            String message = t.getMessage();
            System.err.println(null == message ? "An error occurred." : message);
        } catch (MissingOptionException e) {
            System.err.println(e.getMessage());
            printHelp(options);
        } catch (ParseException e) {
            System.err.println("Unable to parse command line: " + e.getMessage());
            printHelp(options);
        } catch (MalformedURLException e) {
            System.err.println("URL to connect to server is invalid: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Unable to communicate with the server: " + e.getMessage());
        } catch (RuntimeException e) {
            String message = e.getMessage();
            String clazzName = e.getClass().getName();
            System.err.println("A runtime error occurred: " + (null == message ? clazzName : new StringBuilder(clazzName).append(": ").append(message).toString()));
        } catch (Error e) {
            String message = e.getMessage();
            String clazzName = e.getClass().getName();
            System.err.println("A JVM problem occurred: " + (null == message ? clazzName : new StringBuilder(clazzName).append(": ").append(message).toString()));
        } catch (Throwable t) {
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

    @Override
    protected int getAuthFailedExitCode() {
        // Use correct exit code, see com.openexchange.admin.console.BasicCommandlineOptions.SYSEXIT_MISSING_OPTION
        return 104;
    }

    @Override
    protected AuthenticatorMBean getAuthenticator() throws Exception {
        return authenticatorMBean(executionContext);
    }

    /**
     * Gets the {@link AuthenticatorMBean} instance.
     *
     * @param mbsc The MBean server connection
     * @return The {@link AuthenticatorMBean} instance
     * @throws MalformedObjectNameException If generating object name fails
     */
    protected AuthenticatorMBean authenticatorMBean(MBeanServerConnection mbsc) throws MalformedObjectNameException {
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
    @Override
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
     * @throws Exception If authentication fails
     */
    @Override
    protected abstract void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException;

    /**
     * Adds this command-line tool's options.
     * <p>
     * Note following options are reserved:
     * <ul>
     * <li>-h / --help
     * <li>-H / --host
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

    @Override
    protected MBeanServerConnection getContext() {
        return executionContext;
    }

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
    protected static <MBean> MBean getMBean(MBeanServerConnection mbsc, Class<? extends MBean> clazz, String domain) throws MalformedObjectNameException {
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
    protected static ObjectName getObjectName(String className, String domain) throws MalformedObjectNameException {
        int pos = className.lastIndexOf('.');
        return new ObjectName(domain, "name", pos == -1 ? className : className.substring(pos + 1));
    }
}
