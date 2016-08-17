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
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.java.Strings;

/**
 * {@link AbstractRmiCLI} - The abstract helper class for RMI-connecting command-line tools.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.2
 */
public abstract class AbstractRmiCLI<R> extends AbstractAdministrativeCLI<R, String> {

    protected static final AtomicReference<String> RMI_HOSTNAME = new AtomicReference<String>("rmi://localhost:1099/");

    /**
     * Sets the RMI host name
     *
     * @param rmiHostName The RMI host name
     */
    protected static void setRMI_HOSTNAME(final String rmiHostName) {
        String host = rmiHostName;
        if (!host.startsWith("rmi://")) {
            host = "rmi://" + host;
        }
        if (!host.endsWith("/")) {
            host = host + "/";
        }
        RMI_HOSTNAME.set(host);

    }

    // -------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link AbstractRmiCLI}.
     */
    protected AbstractRmiCLI() {
        super();
        setEnvConfigOption("RMI_HOSTNAME");
    }

    private final void setEnvConfigOption(String opt) {
        final String property = System.getProperties().getProperty(opt);
        final String env = System.getenv(opt);
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
    public R execute(final String[] args) {
        Options options = newOptions();
        boolean error = true;
        try {
            // Option for help
            options.addOption("h", "help", false, "Prints a help text");

            // Option for RMI connect
            options.addOption("s", "server", true, "The optional RMI server (default: localhost)");
            options.addOption("p", "port", true, "The optional RMI port (default:1099)");
            options.addOption(new Option(null, "responsetimeout", true, "The optional response timeout in seconds when reading data from server (default: 0s; infinite)"));

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

            // Check for port/server
            String optRmiHostName = null;
            {
                if (cmd.hasOption('p')) {
                    int optPort = parsePort('p', 1099, cmd, options);
                    String optServer = cmd.getOptionValue('s');
                    if (optPort > 0 || null != optServer) {
                        optRmiHostName = "rmi://" + (null == optServer ? "localhost" : optServer) + ":" + (optPort > 0 ? optPort : 1099) + "/";
                    }
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
                    RemoteAuthenticator authenticator = authenticatorStub(optRmiHostName);
                    if (isAuthEnabled(authenticator)) {
                        // Options for administrative authentication
                        String adminLogin = cmd.getOptionValue('A');
                        if (Strings.isEmpty(adminLogin)) {
                            System.out.println("You must provide administrative credentials to proceed.");
                            printHelp(options);
                            System.exit(-1);
                            return null;
                        }

                        String adminPassword = cmd.getOptionValue('P');
                        if (Strings.isEmpty(adminPassword)) {
                            System.out.println("You must provide administrative credentials to proceed.");
                            printHelp(options);
                            System.exit(-1);
                            return null;
                        }

                        administrativeAuth(adminLogin, adminPassword, cmd, authenticator);
                    }
                }
                retval = invoke(options, cmd, optRmiHostName);
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
        } catch (final ExecutionFault e) {
            final Throwable t = e.getCause();
            final String message = t.getMessage();
            System.err.println(null == message ? "An error occurred." : message);
        } catch (final ParseException e) {
            System.err.println("Unable to parse command line: " + e.getMessage());
            printHelp(options);
        } catch (final NotBoundException e) {
            System.err.println("Remote stub not found: " + e.getMessage());
        } catch (final MalformedURLException e) {
            System.err.println("URL to connect to server is invalid: " + e.getMessage());
        } catch (final ConnectException e) {
            System.err.println("Unable to connect to server");
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

    /**
     * Checks if authentication is enabled.
     * <p>
     * By default property <code>"MASTER_AUTHENTICATION_DISABLED"</code> gets examined.
     *
     * @param authenticator The authenticator stub
     * @throws RemoteException If operation fails
     */
    protected boolean isAuthEnabled(RemoteAuthenticator authenticator) throws RemoteException {
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
     * @param authenticator The authenticator stub
     * @throws RemoteException If operation fails
     */
    protected abstract void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException;

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

        Stub stub = (Stub) Naming.lookup(host + name);
        return stub;
    }

}
