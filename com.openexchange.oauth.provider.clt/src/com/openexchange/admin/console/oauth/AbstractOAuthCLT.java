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

package com.openexchange.admin.console.oauth;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.CLIIllegalOptionValueException;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.CLIParseException;
import com.openexchange.admin.console.CLIUnknownOptionException;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagementException;

/**
 * {@link AbstractOAuthCLT}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.0
 */
public class AbstractOAuthCLT extends BasicCommandlineOptions {

    @SuppressWarnings("hiding")
    protected static final String OPT_NAME_ADMINPASS_DESCRIPTION = "Master admin password";
    @SuppressWarnings("hiding")
    protected static final String OPT_NAME_ADMINUSER_DESCRIPTION = "Master admin username";

    private static final String CLIENT_ID_LONG = "id";
    CLIOption clientID = null;

    @Override
    protected void setDefaultCommandLineOptionsWithoutContextID(final AdminParser parser) {
        setAdminUserOption(parser, OPT_NAME_ADMINUSER_LONG, OPT_NAME_ADMINUSER_DESCRIPTION);
        setAdminPassOption(parser, OPT_NAME_ADMINPASS_LONG, OPT_NAME_ADMINPASS_DESCRIPTION);
    }

    /**
     * The default exception handler
     */
    private static BiConsumer<Exception, AdminParser> DEFAULT_EXCEPTION_HANDLER = (e, parser) -> {
        printError(e.getMessage(), parser);
        sysexit(1);
    };

    private static Map<Class<? extends Exception>, BiConsumer<Exception, AdminParser>> exceptionHandlers;
    static {
        Map<Class<? extends Exception>, BiConsumer<Exception, AdminParser>> map = new HashMap<>();
        map.put(CLIParseException.class, (e, parser) -> {
            printError("Parsing command-line failed : " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        });
        map.put(CLIIllegalOptionValueException.class, (e, parser) -> {
            printError("Illegal option value : " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        });
        map.put(CLIUnknownOptionException.class, (e, parser) -> {
            printError("Unrecognized options on the command line: " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        });
        map.put(MissingOptionException.class, (e, parser) -> {
            printError(e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        });
        map.put(MalformedURLException.class, (e, parser) -> {
            printServerException(e, parser);
            sysexit(1);
        });
        map.put(RemoteException.class, (e, parser) -> {
            printServerException(e, parser);
            sysexit(SYSEXIT_REMOTE_ERROR);
        });
        map.put(RemoteClientManagementException.class, (e, parser) -> {
            printServerException(e, parser);
            sysexit(BasicCommandlineOptions.SYSEXIT_COMMUNICATION_ERROR);
        });
        map.put(InvalidCredentialsException.class, (e, parser) -> {
            printServerException(e, parser);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        });
        map.put(IOException.class, (e, parser) -> {
            printError(e.getMessage(), parser);
            sysexit(1);
        });
        map.put(NotBoundException.class, (e, parser) -> {
            printError(e.getMessage(), parser);
            sysexit(1);
        });

        exceptionHandlers = Collections.unmodifiableMap(map);
    }

    /**
     * Initializes a new {@link AbstractOAuthCLT}.
     */
    public AbstractOAuthCLT() {
        super();
    }

    /**
     * Sets the client id option to the specified admin parser
     *
     * @param parser The admin parser
     */
    void setClientIdOption(AdminParser parser) {
        this.clientID = setLongOpt(parser, CLIENT_ID_LONG, "id", "The id of the oauth client", true, true, false);
    }

    /**
     * Retrieves the {@link Credentials} from the specified user and pass options
     *
     * @param adminUserOption The admin user option
     * @param adminPassOption The admin password option
     * @param parser The admin parser
     * @return The credentials
     * @throws CLIIllegalOptionValueException if any of the options has an empty value
     */
    Credentials getCredentials(CLIOption adminUserOption, CLIOption adminPassOption, AdminParser parser) throws CLIIllegalOptionValueException {
        return new Credentials(checkEmpty(adminUserOption, String.class.cast(parser.getOptionValue(adminUserOption))), checkEmpty(adminPassOption, String.class.cast(parser.getOptionValue(adminPassOption))));
    }

    /**
     * Retrieves the client identifier from the specified parser
     *
     * @param clientIDOption The client identifier option
     * @param parser The admin parser
     * @return The client identifier
     * @throws CLIIllegalOptionValueException if the option value is empty
     */
    String getClientId(CLIOption clientIDOption, AdminParser parser) throws CLIIllegalOptionValueException {
        return checkEmpty(clientIDOption, String.class.cast(parser.getOptionValue(clientIDOption)));
    }

    /**
     * Retrieves the context group from the specified option
     *
     * @param contextGroupOption The context group option
     * @param parser The admin parser
     * @return The context group option or the {@link RemoteClientManagement.DEFAULT_GID} if the option's value is empty
     */
    String getContextGroup(CLIOption contextGroupOption, AdminParser parser) {
        String contextGroup = String.class.cast(parser.getOptionValue(contextGroupOption));
        if (null == contextGroup || contextGroup.isEmpty()) {
            contextGroup = RemoteClientManagement.DEFAULT_GID;
        }
        return contextGroup;
    }

    /**
     * Retrieves the {@link RemoteClientManagement}.
     *
     * @param parser The {@link AdminParser}
     * @return The {@link RemoteClientManagement}, never <code>null</code>.
     *         If the client cannot be retrieved, the JVM is terminated.
     */
    RemoteClientManagement getRemoteClientManagement(AdminParser parser) {
        try {
            RemoteClientManagement remote = RemoteClientManagement.class.cast(Naming.lookup(RMI_HOSTNAME + RemoteClientManagement.RMI_NAME));
            if (null != remote) {
                return remote;
            }

            System.err.println("Unable to connect to RMI.");
            sysexit(1);
            return null;
        } catch (Exception e) {
            handleException(e, parser);
            return null;
        }
    }

    /**
     * Handles the specified exception
     *
     * @param e The exception to handle
     * @param parser The {@link AdminParser}
     */
    void handleException(Exception e, AdminParser parser) {
        exceptionHandlers.getOrDefault(e.getClass(), DEFAULT_EXCEPTION_HANDLER).accept(e, parser);
    }

    /**
     * Checks if an argument string is null or empty
     *
     * @param opt
     * @param str
     * @return the string if not empty or null
     * @throws CLIIllegalOptionValueException
     */
    String checkEmpty(CLIOption opt, String str) throws CLIIllegalOptionValueException {
        if (null == str || str.isEmpty()) {
            throw new CLIIllegalOptionValueException(opt, str);
        }
        return str;
    }

    /**
     * Checks if the object is <code>null</code>. Furthermore, the JVM
     * will terminate if the object is <code>null</code> and
     * will print the specified error message to the console.
     *
     * @param object The object to check
     * @param errorMessage The error message to print if the object is <code>null</code>
     */
    void nullCheck(Object object, String errorMessage) {
        if (null != object) {
            return;
        }
        System.out.println(errorMessage);
        sysexit(1);
    }

    /**
     * Convenience method for closing an I/O resource quietly.
     *
     * @param closeable The I/O resource to close.
     */
    static void closeQuietly(Closeable closeable) {
        if (null == closeable) {
            return;
        }
        try {
            closeable.close();
        } catch (@SuppressWarnings("unused") IOException e) {
            // Ignore
        }
    }
}
