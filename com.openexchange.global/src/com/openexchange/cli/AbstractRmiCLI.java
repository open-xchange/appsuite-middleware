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

import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.openexchange.auth.rmi.RemoteAuthenticator;

/**
 * {@link AbstractRmiCLI} - The abstract helper class for RMI-connecting command-line tools.
 *
 * @param <R> - The return type
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.6.2
 */
public abstract class AbstractRmiCLI<R> extends AbstractAdministrativeCLI<R, String, RemoteAuthenticator> {

    protected final static String BASIC_USAGE = "[--responsetimeout <responseTimeout>] | [-h]";
    protected final static String BASIC_MASTER_ADMIN_USAGE = "-A <masterAdmin> -P <masterAdminPassword> [-p <RMI-Port>] [-s <RMI-Server>] " + BASIC_USAGE;
    protected final static String BASIC_CONTEXT_ADMIN_USAGE = "-A <masterAdmin | contextAdmin> -P <masterAdminPassword | contextAdminPassword> [-p <RMI-Port>] [-s <RMI-Server] " + BASIC_USAGE;
    protected final static String BASIC_CONTEXT_ONLY_ADMIN_USAGE = "-A <contextAdmin> -P <contextAdminPassword> [-p <RMI-Port>] [-s <RMI-Server] " + BASIC_USAGE;

    protected final static AtomicReference<String> RMI_HOSTNAME = new AtomicReference<String>("rmi://localhost:1099/");

    /**
     * Sets the RMI host name
     *
     * @param rmiHostName The RMI host name
     */
    protected static void setRMI_HOSTNAME(String rmiHostName) {
        String host = rmiHostName;
        if (!host.startsWith("rmi://")) {
            host = "rmi://" + host;
        }
        if (!host.endsWith("/")) {
            host = host + "/";
        }
        RMI_HOSTNAME.set(host);

    }

    private String executionContext = RMI_HOSTNAME.get();

    // -------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link AbstractRmiCLI}.
     */
    protected AbstractRmiCLI() {
        super();
        setEnvConfigOption("RMI_HOSTNAME");
    }

    private void setEnvConfigOption(String opt) {
        String property = System.getProperties().getProperty(opt);
        String env = System.getenv(opt);
        String setOpt = null;
        if (null != env && env.trim().length() > 0) {
            setOpt = env;
        } else if (null != property && property.trim().length() > 0) {
            setOpt = property;
        }
        if (setOpt != null) {
            if (opt.equals("RMI_HOSTNAME")) {
                setRMI_HOSTNAME(setOpt);
            }
        }
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

            // Option for RMI connect
            options.addOption(createArgumentOption("s", "server", "rmiHost", "The optional RMI server (default: localhost)", false));
            options.addOption(createArgumentOption("p", "port", "rmiPort", "The optional RMI port (default:1099)", false));
            options.addOption(createArgumentOption(null, "responsetimeout", "timeout", "The optional response timeout in seconds when reading data from server (default: 0s; infinite)", false));

            // Check if administrative permission is required and add the admin options if necessary
            boolean requiresAdministrativePermission = optAdministrativeOptions(args);

            // Add other options
            addOptions(options);

            // Check if help output is requested
            boolean helpRequested = helpRequested(args);
            if (helpRequested) {
                printHelp(options);
                System.exit(0);
                return null;
            }

            // Initialize command-line parser & parse arguments
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            // Check for port/server
            if (cmd.hasOption('p')) {
                int optPort = parsePort('p', 1099, cmd, options);
                String optServer = cmd.getOptionValue('s');
                if (optPort > 0 || null != optServer) {
                    executionContext = "rmi://" + (null == optServer ? "localhost" : optServer) + ":" + (optPort > 0 ? optPort : 1099) + "/";
                }
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

            R retval = null;
            try {
                if (requiresAdministrativePermission) {
                    optAuthenticate(cmd);
                }
                retval = invoke(options, cmd, executionContext);
            } catch (MalformedURLException x) {
                throw x;
            } catch (NotBoundException x) {
                throw x;
            } catch (ConnectException x) {
                throw x;
            } catch (Exception e) {
                Throwable t = e.getCause();
                throw new ExecutionFault(null == t ? e : t);
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
        } catch (NotBoundException e) {
            System.err.println("Remote stub not found: " + e.getMessage());
        } catch (MalformedURLException e) {
            System.err.println("URL to connect to server is invalid: " + e.getMessage());
        } catch (ConnectException e) {
            System.err.println("Unable to connect to server");
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
        return -1;
    }

    @Override
    protected RemoteAuthenticator getAuthenticator() throws Exception {
        return authenticatorStub(executionContext);
    }

    /**
     * Gets the {@link RemoteAuthenticator} instance.
     *
     * @param optRmiHostName The optional RMI host name
     * @return The {@link RemoteAuthenticator} instance
     * @throws NotBoundException If name is not currently bound
     * @throws RemoteException If registry could not be contacted
     * @throws AccessException If this operation is not permitted
     * @throws MalformedURLException If the name is not an appropriately formatted URL
     */
    protected RemoteAuthenticator authenticatorStub(String optRmiHostName) throws RemoteException, MalformedURLException, NotBoundException {
        return getRmiStub(optRmiHostName, RemoteAuthenticator.RMI_NAME);
    }

    @Override
    protected boolean isAuthEnabled(RemoteAuthenticator authenticator) throws RemoteException {
        return !authenticator.isMasterAuthenticationDisabled();
    }

    /**
     * Adds this command-line tool's options.
     * <p>
     * Note following options are reserved:
     * <ul>
     * <li>-h / --help
     * <li>-s / --server
     * <li>-p / --port
     * <li>-A / --adminuser
     * <li>-P / --adminpass
     * </ul>
     *
     * @param options The options
     */
    @Override
    protected abstract void addOptions(Options options);

    /**
     * Invokes the RMI method.
     *
     * @param options The options
     * @param cmd The command line providing parameters/options
     * @param optRmiHostName The optional RMI host name
     * @return The return value
     * @throws Exception If invocation fails
     */
    @Override
    protected abstract R invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception;

    @Override
    protected String getContext() {
        return executionContext;
    }

    /**
     * Gets the specified RMI reference/stub for given name using default RMI host.
     *
     * @param name The stub's name
     * @return The RMI reference/stub
     * @throws NotBoundException If name is not currently bound
     * @throws RemoteException If registry could not be contacted
     * @throws AccessException If this operation is not permitted
     * @throws MalformedURLException If the name is not an appropriately formatted URL
     */
    protected static <Stub extends Remote> Stub getRmiStub(String name) throws MalformedURLException, RemoteException, NotBoundException {
        return getRmiStub(RMI_HOSTNAME.get(), name);
    }

    /**
     * Gets the specified RMI reference/stub for given name using given optional RMI host.
     *
     * @param optRmiHostName The optional RMI host name (<code>null</code> falls back to {@link #RMI_HOSTNAME})
     * @param name The stub's name
     * @return The RMI reference/stub
     * @throws NotBoundException If name is not currently bound
     * @throws RemoteException If registry could not be contacted
     * @throws AccessException If this operation is not permitted
     * @throws MalformedURLException If the name is not an appropriately formatted URL
     */
    protected static <Stub extends Remote> Stub getRmiStub(String optRmiHostName, String name) throws MalformedURLException, RemoteException, NotBoundException {
        String host = optRmiHostName;
        if (null == host) {
            host = RMI_HOSTNAME.get();
        }
        if (!host.startsWith("rmi://")) {
            host = "rmi://" + host;
        }
        if (!host.endsWith("/")) {
            host = host + "/";
        }

        @SuppressWarnings("unchecked") Stub stub = (Stub) Naming.lookup(host + name);
        return stub;
    }
}
